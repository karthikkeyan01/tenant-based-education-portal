package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dto.audit.AuditRequestDto;
import com.fts.tenantbasededuportal.dto.user.*;
import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.ConflictException;
import com.fts.tenantbasededuportal.util.constants.*;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.exception.ResourceNotFoundException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.OrganizationRepository;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final PermissionService permissionService;
    private final PasswordGeneratorService passwordGeneratorService;
    private final TokenGeneratorService tokenGeneratorService;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    //Performs a GET operation and fetches all users from the DB.
    public Page<UserResponseDto> retrieveUsers(final int page, final int size) {

        this.permissionService.requirePermission(PermissionConstants.VIEW_USERS);

        final PageRequest pageRequest = PageRequest.of(page, size,Sort.by("createdAt").descending());
        final Page<User> users;

        //checks if the logged-in user is super admin if so allows the operation.
        if(this.securityUtil.isSuperAdmin()){
            users = this.userRepository.findByActiveTrueAndIdNot (this.securityUtil.getCurrentUserId(), pageRequest);
        }
        //checks if the logged-in user is org admin if so allows the operation.
        else if (this.securityUtil.isOrgAdmin()) {
            users = this.userRepository.findByOrganizationAndActiveTrueAndIdNot(this.securityUtil.getCurrentOrganization(),
                    this.securityUtil.getCurrentUserId(), pageRequest);
        }
        //deny the operation for users.
        else {
            throw new UnauthorizedException("you don't have permission to view users");
        }

        return users.map(user ->
                UserResponseDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roleName(user.getRole().getName())
                        .mfaEnabled(user.getMfaEnabled())
                        .active(user.getActive())
                        .organizationName(user.getOrganization() != null
                                ? user.getOrganization().getName()
                                : null)
                        .createdAt(user.getCreatedAt())
                        .build());
    }

    //Performs a GET operation and fetches the user based on the given id.
    public UserResponseDto retrieveUserById(final String id){

        this.permissionService.requirePermission(PermissionConstants.VIEW_USERS);

        if (this.securityUtil.isCurrentUser(id)){
            throw  new BadRequestException("You can't view own data using this endpoint");
        }

        final User targetUser = this.userRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new ResourceNotFoundException(
                                        "User not found."));
        //if role is org admin then they can only view users.
        if (this.securityUtil.isOrgAdmin() && !RoleConstants.USER.equals(targetUser.getRole().getName())) {
            throw new UnauthorizedException("Organization admins can only view users.");
        }
        //checks if the current user is org admin.
        if (this.securityUtil.isOrgAdmin()) {
            /*checks the role of target user is null and compares it with
            the role of current user (both roles should match) (false and true,
             true and false or true and true gives exception).
             */
            if (targetUser.getOrganization() == null || !this.securityUtil.isSameOrganization(targetUser.getOrganization().getId())) {
                throw new UnauthorizedException("You cannot access users from another organization.");
            }
        }

        return UserResponseDto.builder()
                .id(targetUser.getId())
                .email(targetUser.getEmail())
                .firstName(targetUser.getFirstName())
                .lastName(targetUser.getLastName())
                .roleName(targetUser.getRole().getName())
                .organizationName(targetUser.getOrganization() != null
                        ? targetUser.getOrganization().getName()
                        : null)
                .mfaEnabled(targetUser.getMfaEnabled())
                .active(targetUser.getActive())
                .createdAt(targetUser.getCreatedAt())
                .build();
    }

    public Page<UserResponseDto> retrieveUsersByOrganization(final String id, final int page, final int size) {

        if (!this.securityUtil.isSuperAdmin()){
            throw new UnauthorizedException("only super admin has permission to view users by organization.");
        }
        this.permissionService.requirePermission(PermissionConstants.VIEW_USERS);

        final Organization organization = this.organizationRepository.findById(id).orElseThrow(() ->
                        new ResourceNotFoundException("Organization not found."));
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        final Page<User> users = this.userRepository.findByOrganizationAndActiveTrue(organization, pageRequest);

        return users.map(user ->
                UserResponseDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roleName(user.getRole().getName())
                        .organizationName(organization.getName())
                        .mfaEnabled(user.getMfaEnabled())
                        .active(user.getActive())
                        .createdAt(user.getCreatedAt())
                        .build());
    }

    public Page<UserResponseDto> retrieveIndividualUsers(final int page, final int size) {

        if (!this.securityUtil.isSuperAdmin()){
            throw new UnauthorizedException("Only the Super Admin has permission to view individual users.");
        }
        this.permissionService.requirePermission(PermissionConstants.VIEW_USERS);

        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        final Page<User> users = this.userRepository.findByOrganizationIsNullAndActiveTrueAndIdNot(
                this.securityUtil.getCurrentUserId(), pageRequest);

        return users.map(user ->
                UserResponseDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roleName(user.getRole().getName())
                        .mfaEnabled(user.getMfaEnabled())
                        .active(user.getActive())
                        .createdAt(user.getCreatedAt())
                        .build());
    }

    //Performs a POST operation and creates a user in DB.
    @Transactional
    public UserResponseDto createUser(final CreateUserRequestDto request){

        this.permissionService.requirePermission(PermissionConstants.CREATE_USER);
        if (!this.securityUtil.isOrgAdmin()){
            throw new UnauthorizedException("Only organization admins can create users.");
        }

        //checks if the user already exists with email.
        if (this.userRepository.existsByEmail(request.getEmail())){
            throw new ConflictException("Email already exists.");
        }

        final Role role = this.roleRepository.findByName(RoleConstants.USER).orElseThrow(()->
                new IllegalStateException("Role not found."));
        final Organization organization = this.securityUtil.getCurrentOrganization();
        final String temporaryPassword = this.passwordGeneratorService.generatePassword(
                ApplicationConstants.GENERATED_PASSWORD_LENGTH);
        final String activationToken = this.tokenGeneratorService.generateToken();
        final User user = User.builder()
                .email(request.getEmail())
                .password(this.passwordEncoder.encode(temporaryPassword))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(role)
                .organization(organization)
                .active(false)
                .mfaEnabled(false)
                .activationToken(activationToken)
                .activationTokenExpiresAt(
                        Instant.now().plus(ApplicationConstants.ACTIVATION_LINK_EXPIRY_HOURS
                                , ChronoUnit.HOURS))
                .build();

        final User savedUser = this.userRepository.save(user);
        final String activationLink = this.baseUrl + "/auth/activate-account?token=" + activationToken;

        this.emailService.sendActivationMail(savedUser.getEmail(), activationLink);
        this.auditService.create(AuditRequestDto.builder()
                        .action(AuditActionConstants.CREATE_USER)
                        .entityAffected(EntityAffectedConstants.USER)
                        .entityId(savedUser.getId())
                        .description("Created user: " + savedUser.getEmail())
                        .build());

        return UserResponseDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .roleName(savedUser.getRole().getName())
                .organizationName(savedUser.getOrganization().getName())
                .mfaEnabled(savedUser.getMfaEnabled())
                .active(savedUser.getActive())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Transactional
    public void activate(final String userId, final boolean active){

        if (!this.securityUtil.isOrgAdmin()){
            throw new UnauthorizedException("Only organization admins can activate users.");
        }
        this.permissionService.requirePermission(PermissionConstants.MANAGE_USER);

        if(this.securityUtil.isCurrentUser(userId)){
            throw new BadRequestException(
                    "You cannot activate or deactivate your own account.");
        }

        final User targetUser =  this.userRepository.findById(userId).orElseThrow(()->
                new ResourceNotFoundException("User not found."));

        if (targetUser.getOrganization() == null || !this.securityUtil.isSameOrganization(targetUser.getOrganization().getId())) {
            throw new UnauthorizedException("You can only manage users in your own organization.");
        }
        if (!RoleConstants.USER.equals(targetUser.getRole().getName())) {
            throw new UnauthorizedException("Only users can be activated or deactivated.");
        }
        if (targetUser.getActive().equals(active)) {
            throw new BadRequestException(active
                    ? "User is already active."
                    : "User is already inactive.");
        }

        targetUser.setActive(active);
        this.userRepository.save(targetUser);
        this.auditService.create(
                AuditRequestDto.builder()
                        .action(active
                                ? AuditActionConstants.ACTIVATE_USER
                                : AuditActionConstants.DEACTIVATE_USER)
                        .entityAffected(EntityAffectedConstants.USER)
                        .entityId(targetUser.getId())
                        .description(active
                                ? "Activated user: " + targetUser.getEmail()
                                : "Deactivated user: " + targetUser.getEmail())
                        .build());
    }

    @Transactional
    public void activate(final String id, final boolean active, final boolean isUser){

        if (!this.securityUtil.isSuperAdmin()){
            throw new UnauthorizedException("Only super admins can perform this operation.");
        }
        if (isUser){
            this.permissionService.requirePermission(PermissionConstants.MANAGE_USER);
        }
        else {
            this.permissionService.requirePermission(PermissionConstants.MANAGE_ORGANIZATION);
        }

        if (isUser){
            final User targetUser = this.userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException(
                    "User not found."));

            if (this.securityUtil.isCurrentUser(id)){
                throw new BadRequestException("You cannot activate or deactivate your own account.");
            }
            if (targetUser.getOrganization() != null){
                throw new BadRequestException("Organization users cannot be activated or deactivated individually.");
            }

            if (targetUser.getActive().equals(active)){
                throw new BadRequestException(active
                        ? "User is already active."
                        : "User is already inactive.");
            }

            targetUser.setActive(active);
            this.userRepository.save(targetUser);
            this.auditService.create(
                    AuditRequestDto.builder()
                            .action(active
                            ? AuditActionConstants.ACTIVATE_USER
                            : AuditActionConstants.DEACTIVATE_USER)
                            .entityAffected(EntityAffectedConstants.USER)
                            .entityId(targetUser.getId())
                            .description(active
                                    ? "Activated user: " + targetUser.getEmail()
                                    : "Deactivated user: " + targetUser.getEmail())
                            .build());
        }
        else {
            final Organization organization = this.organizationRepository.findById(id).orElseThrow(() ->
                                    new ResourceNotFoundException("Organization not found."));

            if (organization.getActive().equals(active)){
                throw new BadRequestException(active
                        ? "Organization is already active."
                        : "Organization is already inactive.");
            }

            organization.setActive(active);
            this.organizationRepository.save(organization);
            final List<User> users = this.userRepository.findByOrganization(organization);

            for (final User user : users){
                user.setActive(active);
            }
            this.userRepository.saveAll(users);

            this.auditService.create(
                    AuditRequestDto.builder()
                            .action(active
                                    ? AuditActionConstants.ACTIVATE_ORGANIZATION
                                    : AuditActionConstants.DEACTIVATE_ORGANIZATION)
                            .entityAffected(EntityAffectedConstants.ORGANIZATION)
                            .entityId(organization.getId())
                            .description(active
                                    ? "Activated organization: " + organization.getName()
                                    : "Deactivated organization: " + organization.getName())
                            .build());
        }
    }

    //performs a PUT operation and bulk uploads users to DB.
    //only org admins can bulk upload.
    @Transactional
    public BulkUploadResponseDto bulkUploadUsers(final MultipartFile file, final String organizationId){

        if (file.isEmpty()) {
            throw new BadRequestException("File is empty.");
        }
        //checks if current user is super admin or org admin.
        if (!this.securityUtil.isSuperAdmin() && !this.securityUtil.isOrgAdmin()){
            throw new UnauthorizedException("You are not authorized to bulk upload users.");
        }
        this.permissionService.requirePermission(PermissionConstants.CREATE_USER);

        final Organization targetOrganization;
        if (this.securityUtil.isSuperAdmin()){
            if (organizationId == null || organizationId.isBlank()){
                throw new BadRequestException("organization Id is required");
            }

            targetOrganization = organizationRepository.findByIdAndActiveTrue(organizationId)
                    .orElseThrow(()-> new ResourceNotFoundException(
                            "Organization not found"));
        }
        else {
            targetOrganization = this.securityUtil.getCurrentOrganization();
        }

        final String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException("invalid file");
        }
        if (fileName.toLowerCase().endsWith(".csv")){
            return this.uploadCsv(file, targetOrganization);
        }
        if (fileName.toLowerCase().endsWith(".xlsx")){
            return this.uploadExcel(file, targetOrganization);
        }
        throw new BadRequestException("Only CSV and XLSX files are supported.");
    }

    //method for uploading/restoring csv called by bulk upload and restore.
    //takes file, current user, a boolean restore and organization id as parameters based on the operation.
    private BulkUploadResponseDto uploadCsv(final MultipartFile file, final Organization targetOrganization ) {

        final Role userRole = this.roleRepository.findByName(RoleConstants.USER).orElseThrow(()->
                new IllegalStateException("Role not found."));

        //sets variables needed for response dto.
        int total = 0;
        int uploaded = 0;
        int skipped = 0;
        int emailFailed = 0;

        final List<String> failedEmails = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String line;
            final String headerLineBom = br.readLine();

            //makes sure header file is present.
            if (headerLineBom == null) {
                throw new BadRequestException("CSV file is empty.");
            }

            final String headerLine = headerLineBom.replace("\uFEFF", "").trim();
            final String header = headerLine.trim();

            //checks the header order and throws error if the order is not one of the given orders.
            if (!header.equalsIgnoreCase("email") && !header.equalsIgnoreCase("email,firstName,lastName")) {
                throw new BadRequestException("Invalid CSV header.");
            }

            //main loop runs till the next line in file is null.
            while ((line = br.readLine()) != null){
                if (line.isBlank()) {
                    continue;
                }
                total++;
                final String[] values = line.split(",");

                //can only have values of length of format mentioned above.
                if (header.equalsIgnoreCase("email")) {
                    if (values.length != 1) {
                        skipped++;
                        continue;
                    }
                } else {
                    if (values.length != 3) {
                        skipped++;
                        continue;
                    }
                }

                final String email = values[0].trim();
                String firstName = null;
                String lastName = null;

                if (values.length == 3) {
                    firstName = values[1].trim();
                    lastName = values[2].trim();
                }
                if (email.isBlank()) {
                    skipped++;
                    continue;
                }
                if (!email.matches(EMAIL_REGEX)) {
                    skipped++;
                    failedEmails.add(email);
                    continue;
                }
                if (values.length == 3 && (firstName.isBlank() || lastName.isBlank())) {
                    skipped++;
                    continue;
                }
                if (this.userRepository.existsByEmail(email)) {
                    skipped++;
                    continue;
                }

                final String temporaryPassword = this.passwordGeneratorService.
                        generatePassword(ApplicationConstants.GENERATED_PASSWORD_LENGTH);
                final String activationToken = this.tokenGeneratorService.generateToken();
                final User user = User.builder()
                            .email(email)
                            .firstName(firstName)
                            .lastName(lastName)
                            .password(this.passwordEncoder.encode(temporaryPassword))
                            .role(userRole)
                            .organization(targetOrganization)
                            .active(false)
                            .mfaEnabled(false)
                            .activationToken(activationToken)
                            .activationTokenExpiresAt(Instant.now().plus(
                                    ApplicationConstants
                                            .ACTIVATION_LINK_EXPIRY_HOURS, ChronoUnit.HOURS))
                            .build();

                final User savedUser = this.userRepository.save(user);
                uploaded++;
                final String activationLink = this.baseUrl + "/auth/activate-account?token=" + activationToken;

                try {
                    this.emailService.sendActivationMail(savedUser.getEmail(), activationLink);
                }
                catch (final Exception exception) {

                    emailFailed++;
                    failedEmails.add(savedUser.getEmail());
                }
            }

            this.auditService.create(
                    AuditRequestDto.builder()
                            .action(AuditActionConstants.BULK_UPLOAD_USERS)
                            .entityAffected(EntityAffectedConstants.USER)
                            .description("Bulk uploaded "
                            + uploaded
                            + " users to organization: "
                            + targetOrganization.getName())
                            .build());

            return BulkUploadResponseDto.builder()
                    .totalRecords(total)
                    .uploadedRecords(uploaded)
                    .skippedRecords(skipped)
                    .emailFailedRecords(emailFailed)
                    .failedEmails(failedEmails)
                    .build();

        }
        catch (final BadRequestException exception) {
            throw exception;
        }

        catch (final Exception exception) {
            throw new BadRequestException("Invalid CSV file.");
        }
    }

    //method for bulk upload and restore users.
    private BulkUploadResponseDto uploadExcel(final MultipartFile file, final Organization targetOrganization) {

        final Role userRole = this.roleRepository.findByName(RoleConstants.USER).orElseThrow(()->
                new IllegalStateException("Role not found."));

        int total = 0;
        int uploaded = 0;
        int skipped = 0;
        int emailFailed = 0;

        final List<String> failedEmails = new ArrayList<>();

        try(InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {

            final DataFormatter formatter = new DataFormatter();
            final Sheet sheet = workbook.getSheetAt(0);
            final Row headerRow = sheet.getRow(0);

            if(headerRow == null){
                throw new BadRequestException("Excel file is empty.");
            }

            final String column1 = formatter.formatCellValue(headerRow.getCell(0)).trim();
            String column2 = null;
            String column3 = null;
            boolean oneColumnFormat = headerRow.getLastCellNum() == 1 && column1.equalsIgnoreCase("email");

            if (headerRow.getCell(1) != null) {
                column2 = formatter.formatCellValue(
                        headerRow.getCell(1)).trim();
            }
            if (headerRow.getCell(2) != null) {
                column3 = formatter.formatCellValue(
                        headerRow.getCell(2)).trim();
            }

            boolean threeColumnFormat = headerRow.getCell(1) != null && headerRow.getCell(2) != null
                            && column1.equalsIgnoreCase("email") && "firstName".equalsIgnoreCase(column2)
                            && "lastName".equalsIgnoreCase(column3);
            //checks both headers.
            if (!oneColumnFormat && !threeColumnFormat) {
                throw new BadRequestException("Invalid Excel header.");
            }

            //main loop.
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                final Row row = sheet.getRow(i);
                if(row == null){
                    continue;
                }
                total++;
                if (oneColumnFormat) {
                    if (row.getCell(0) == null) {
                        skipped++;
                        continue;
                    }
                }
                else {
                    if (row.getCell(0) == null || row.getCell(1) == null || row.getCell(2) == null) {
                        skipped++;
                        continue;
                    }
                }

                final String email = formatter.formatCellValue(row.getCell(0)).trim();
                String firstName = null;
                String lastName =  null;

                if (threeColumnFormat) {
                    firstName = formatter.formatCellValue(row.getCell(1)).trim();
                    lastName = formatter.formatCellValue(row.getCell(2)).trim();
                }
                if (email.isBlank()) {
                    skipped++;
                    continue;
                }
                if (!email.matches(EMAIL_REGEX)) {
                    skipped++;
                    failedEmails.add(email);
                    continue;
                }
                if (threeColumnFormat && (firstName.isBlank() || lastName.isBlank())){
                    skipped++;
                    continue;
                }
                //checks if user is already existing.
                if (this.userRepository.existsByEmail(email)){
                    skipped++;
                    continue;
                }

                final String temporaryPassword = this.passwordGeneratorService
                        .generatePassword(ApplicationConstants.GENERATED_PASSWORD_LENGTH);
                final String activationToken = this.tokenGeneratorService.generateToken();
                final User user = User.builder()
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .password(this.passwordEncoder.encode(temporaryPassword))
                        .role(userRole)
                        .organization(targetOrganization)
                        .active(false)
                        .mfaEnabled(false)
                        .activationToken(activationToken)
                        .activationTokenExpiresAt(Instant.now().plus(
                                ApplicationConstants
                                        .ACTIVATION_LINK_EXPIRY_HOURS, ChronoUnit.HOURS))
                        .build();
                final User savedUser = this.userRepository.save(user);
                uploaded++;
                final String activationLink = this.baseUrl + "/auth/activate-account?token=" + activationToken;

                try {
                    this.emailService.sendActivationMail(savedUser.getEmail(), activationLink);
                }
                catch (final Exception exception){
                    emailFailed++;
                    failedEmails.add(savedUser.getEmail());
                }
            }

            this.auditService.create(
                    AuditRequestDto.builder()
                            .action(AuditActionConstants.BULK_UPLOAD_USERS)
                            .entityAffected(EntityAffectedConstants.USER)
                            .description("Bulk uploaded "
                                    + uploaded
                                    + " users to organization: "
                                    + targetOrganization.getName())
                            .build());

            return BulkUploadResponseDto.builder()
                    .totalRecords(total)
                    .uploadedRecords(uploaded)
                    .skippedRecords(skipped)
                    .emailFailedRecords(emailFailed)
                    .failedEmails(failedEmails)
                    .build();
        }
        catch (final BadRequestException exception) {
            throw exception;
        }

        catch (final Exception exception) {
            throw new BadRequestException("Invalid Excel file.");
        }
    }

    @Transactional
    public void resendActivationEmail(final String email) {

        if (!this.securityUtil.isSuperAdmin() && !this.securityUtil.isOrgAdmin()){
            throw new UnauthorizedException("You are not allowed to resend activation email.");
        }
        this.permissionService.requirePermission(PermissionConstants.RESEND_ACTIVATION_EMAIL);

        final User targetUser = this.userRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("User not found."));

        if (targetUser.getActive()) {
            throw new BadRequestException("User account is already activated.");
        }
        if (this.securityUtil.isOrgAdmin()){
            if (targetUser.getOrganization() == null || !this.securityUtil.isSameOrganization(
                    targetUser.getOrganization().getId())){
                                throw new UnauthorizedException("You can only resend activation emails to users in your own organization.");
            }
            if (!RoleConstants.USER.equals(targetUser.getRole().getName())) {
                throw new UnauthorizedException("Organization admins can only resend activation emails to users.");
            }
        }

        if (this.securityUtil.isSuperAdmin() && RoleConstants.USER.equals(targetUser.getRole().getName())) {
            throw new BadRequestException("Organization users must be managed by their organization administrator.");
        }

        final String activationToken = this.tokenGeneratorService.generateToken();
        targetUser.setActivationToken(activationToken);
        targetUser.setActivationTokenExpiresAt(Instant.now().plus(ApplicationConstants.ACTIVATION_LINK_EXPIRY_HOURS, ChronoUnit.HOURS));

        final User savedUser = this.userRepository.save(targetUser);
        final String activationLink = this.baseUrl + "/auth/activate-account?token=" + activationToken;

        this.emailService.sendActivationMail(savedUser.getEmail(), activationLink);
        this.auditService.create(
                AuditRequestDto.builder()
                        .action(AuditActionConstants.RESEND_ACTIVATION_EMAIL)
                        .entityAffected(EntityAffectedConstants.USER)
                        .entityId(savedUser.getId())
                        .description("Resent activation email to: "
                                + savedUser.getEmail())
                        .build());
    }
}
