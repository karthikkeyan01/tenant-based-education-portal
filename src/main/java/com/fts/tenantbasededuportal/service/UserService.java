package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dto.audit.AuditRequestDto;
import com.fts.tenantbasededuportal.dto.user.*;
import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.enums.PermissionType;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.exception.ResourceNotFoundException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.OrganizationRepository;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.fts.tenantbasededuportal.util.RoleConstants;
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

    //Performs a GET operation and fetches all users from the DB.
    public Page<UserResponseDto> retrieveUsers(final Pageable pageable) {

        this.permissionService.requirePermission(PermissionType.VIEW_USERS);

        final Page<User> users;

        //checks if the logged-in user is super admin if so allows the operation.
        if(this.securityUtil.isSuperAdmin()){

            users = this.userRepository.findByActiveTrueAndIdNot (
                    this.securityUtil.getCurrentUserId(), pageable);
        }
        //checks if the logged-in user is org admin if so allows the operation.
        else if (this.securityUtil.isOrgAdmin()) {

            users = this.userRepository.findByOrganizationAndActiveTrueAndIdNot(
                    this.securityUtil.getCurrentOrganization(),
                    this.securityUtil.getCurrentUserId(), pageable);
        }
        //deny the operation for users.
        else {
            throw new UnauthorizedException(
                    "you don't have permission to view users");
        }

        final List<UserResponseDto> response = new ArrayList<>();

        for(final User user : users.getContent()){

            String organizationName = null;

            if (user.getOrganization() != null){

                organizationName = user.getOrganization().getName();
            }

            response.add(
                    UserResponseDto.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .roleName(user.getRole().getName())
                            .mfaEnabled(user.getMfaEnabled())
                            .active(user.getActive())
                            .organizationName(organizationName)
                            .createdAt(user.getCreatedAt())
                            .build()
            );
        }

        return new PageImpl<>(response,pageable,
                users.getTotalElements());
    }

    //Performs a GET operation and fetches the user based on the given id.
    public UserResponseDto retrieveUserById(final String id){

        this.permissionService.requirePermission(PermissionType.VIEW_USERS);

        if (this.securityUtil.isCurrentUser(id)){

            throw  new BadRequestException(
                    "You can't view own data using this endpoint");
        }

        final User targetUser = this.userRepository.findByIdAndActiveTrue(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "User not found."));

        //if role is org admin then they can only view users.
        if (this.securityUtil.isOrgAdmin()
                && !RoleConstants.USER.equals(targetUser.getRole().getName())) {

            throw new UnauthorizedException
                    ("Organization admins can only view users.");
        }

        //checks if the current user is org admin.
        if (this.securityUtil.isOrgAdmin()) {

            /*checks the role of target user is null and compares it with
            the role of current user (both roles should match) (false and true,
             true and false or true and true gives exception).
             */
            if (targetUser.getOrganization() == null
                    || !this.securityUtil.isSameOrganization(
                            targetUser.getOrganization().getId())) {

                throw new UnauthorizedException(
                        "You cannot access users from another organization.");
            }
        }

        String organizationName = null;

        if (targetUser.getOrganization() != null) {

            organizationName = targetUser.getOrganization().getName();
        }

        return UserResponseDto.builder()
                .id(targetUser.getId())
                .email(targetUser.getEmail())
                .firstName(targetUser.getFirstName())
                .lastName(targetUser.getLastName())
                .roleName(targetUser.getRole().getName())
                .organizationName(organizationName)
                .mfaEnabled(targetUser.getMfaEnabled())
                .active(targetUser.getActive())
                .createdAt(targetUser.getCreatedAt())
                .build();
    }

    //Performs a POST operation and creates a user in DB.
    @Transactional
    public UserResponseDto createUser(final CreateUserRequestDto request){

        this.permissionService.requirePermission(PermissionType
                .CREATE_USER);

        final User currentUser = this.securityUtil.getCurrentUser();

        if (!this.securityUtil.isOrgAdmin()){

            throw new UnauthorizedException(
                    "Only organization admins can create users.");
        }

        //checks if the user already exists with email.
        if (this.userRepository.existsByEmail(request.getEmail())){
            throw new BadRequestException("Email already exists.");
        }

        final Role role = this.roleRepository.findByName(
                RoleConstants.USER).orElseThrow(
                ()-> new ResourceNotFoundException("Role not found."));

        final Organization organization = this.securityUtil.getCurrentOrganization();

        final String temporaryPassword =
                this.passwordGeneratorService.generatePassword(12);

        final String activationToken =
                this.tokenGeneratorService.generateToken();

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
                        Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        final User savedUser = this.userRepository.save(user);

        final String activationLink =
                this.baseUrl
                + "/auth/activate-account?token="
                + activationToken;

        this.emailService.sendActivationMail(
                savedUser.getEmail(), activationLink);

        this.auditService.create(
                AuditRequestDto.builder()
                        .action("CREATE_USER")
                        .entityAffected("USER")
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

            throw new UnauthorizedException(
                    "Only organization admins can activate users.");
        }

        this.permissionService.requirePermission(PermissionType.MANAGE_USER);

        if(this.securityUtil.isCurrentUser(userId)){

            throw new BadRequestException(
                    "You cannot activate or deactivate your own account.");
        }

        final User targetUser =  this.userRepository.findById(userId)
                .orElseThrow(()->
                        new ResourceNotFoundException("User not found."));

        if (targetUser.getOrganization() == null
                || !this.securityUtil.isSameOrganization(
                targetUser.getOrganization().getId())) {

            throw new UnauthorizedException(
                    "You can only manage users in your own organization.");
        }

        if (!RoleConstants.USER.equals(targetUser.getRole().getName())) {

            throw new UnauthorizedException(
                    "Only users can be activated or deactivated.");
        }

        if (targetUser.getActive().equals(active)) {

            throw new BadRequestException(
                    active
                            ? "User is already active."
                            : "User is already inactive.");
        }

        targetUser.setActive(active);

        this.userRepository.save(targetUser);

        this.auditService.create(
                AuditRequestDto.builder()
                        .action(active
                                ? "ACTIVATE_USER"
                                : "DEACTIVATE_USER")
                        .entityAffected("USER")
                        .entityId(targetUser.getId())
                        .description(active
                                ? "Activated user: " + targetUser.getEmail()
                                : "Deactivated user: " + targetUser.getEmail())
                        .build());
    }


//    public void activate(final String id, final boolean active,
//                         final boolean isUser){
//
//        if (!this.securityUtil.isSuperAdmin()){
//
//            throw new UnauthorizedException(
//                    "Only super admins can perform this operation.");
//        }
//
//        this.permissionService.requirePermission(PermissionType.MANAGE_USER);
//
//        final
//    }

    //performs a PUT operation and bulk uploads users to DB.
    //only org admins can bulk upload.
    public BulkUploadResponseDto bulkUploadUsers(
            final MultipartFile file, final String organizationId){

        final User currentUser = this.securityUtil.getCurrentUser();

        final String currentRole = currentUser.getRole().getName();

        //checks if current user is super admin or org admin.
        if (!RoleConstants.SUPER_ADMIN.equals(currentRole)&&
                !RoleConstants.ORG_ADMIN.equals(currentRole)){

            throw new UnauthorizedException(
                    "You are not authorized to bulk upload users.");
        }

        final Organization targetOrganization;

        if (RoleConstants.SUPER_ADMIN.equals(currentRole)){

            if (organizationId == null || organizationId.isBlank()){

                throw new BadRequestException("organization Id is required");
            }

            targetOrganization = organizationRepository.findByIdAndDeletedFalse(organizationId)
                    .orElseThrow(()-> new ResourceNotFoundException("Organization not found"));

        }
        else {

            targetOrganization = currentUser.getOrganization();
        }



        final String fileName = file.getOriginalFilename();

        if (fileName == null) {

            throw new BadRequestException(
                    "File name is missing.");
        }

        if (fileName.endsWith(".csv")){

            return uploadCsv(file, currentUser,  targetOrganization);
        }

        if (fileName.endsWith(".xlsx")){

            return uploadExcel(file, currentUser,  targetOrganization);
        }

        throw new BadRequestException(
                "Only CSV and XLSX files are supported.");
    }

    //method for uploading/restoring csv called by bulk upload and restore.
    //takes file, current user, a boolean restore and organization id as parameters based on the operation.
    private BulkUploadResponseDto uploadCsv
            (final MultipartFile file, final User currentUser,
             final Organization targetOrganization ) {

        final Role userRole = this.roleRepository.findByName(RoleConstants.USER)
                .orElseThrow(()-> new ResourceNotFoundException("Role not found."));

        final List<User> users = new ArrayList<>();

        //sets variables needed for response dto.
        int total = 0;
        int processed = 0;
        int skipped = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))){

            String line;
            String header = br.readLine();

            //makes sure header file is present.
            if (header == null) {

                throw new BadRequestException("CSV file is empty.");
            }

            header = header.trim();

            //checks the header order and throws error if the order is not one of the given orders.
            if (!header.equalsIgnoreCase("email,password")
            && !header.equalsIgnoreCase("email,password,firstName,secondName")) {

                throw new BadRequestException("Invalid CSV header.");
            }

            //main loop runs till the next line in file is null.
            while ((line = br.readLine()) != null){

                total++;

                String[] values = line.split(",");

                //can only have values of length of format mentioned above.
                if (values.length != 2 && values.length != 4) {

                    skipped++;
                    continue;
                }

                String email = values[0].trim();

                String password = values[1].trim();

                String firstName = null;

                String secondName = null;

                if (values.length == 4) {

                    firstName = values[2].trim();

                    secondName = values[3].trim();
                }

                final User existingUser = this.userRepository.findByEmail(email)
                        .orElse(null);

                if (existingUser != null){

                    skipped++;
                    continue;
                }

                User user = User.builder()
                        .email(email)
                        .firstName(firstName)
                        .secondName(secondName)
                        .password(this.passwordEncoder.encode(password))
                        .role(userRole)
                        .organization(targetOrganization)
                        .deleted(false)
                        .mfaEnabled(false)
                        .build();

                users.add(user);

                processed++;
            }

            this.userRepository.saveAll(users);

            this.auditService.log(
                    currentUser,
                    "BULK_UPLOAD_USERS",
                    "USER",
                    null,
                    "Bulk uploaded " + processed + " users from CSV");

            return BulkUploadResponseDto.builder()
                    .totalRecords(total)
                    .processedRecords(processed)
                    .skippedRecords(skipped)
                    .build();

        }
        catch (BadRequestException e) {
            throw e;
        }

        catch (Exception e) {

            throw new BadRequestException("Invalid CSV file.");
        }
    }

    //method for bulk upload and restore users.
    private BulkUploadResponseDto uploadExcel
            (final MultipartFile file, final User currentUser,
             final Organization targetOrganization) {

        final Role userRole = this.roleRepository.findByName(RoleConstants.USER)
                .orElseThrow(()-> new ResourceNotFoundException("Role not found."));

        final List<User> users = new ArrayList<>();

        int total = 0;
        int processed = 0;
        int skipped = 0;

        try(InputStream is = file.getInputStream();
                Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);

            if(headerRow == null){

                throw new BadRequestException("Excel file is empty.");
            }

            if (headerRow.getCell(0) == null || headerRow.getCell(1) == null) {

                throw new BadRequestException("Invalid Excel header.");
            }

            String column1 = headerRow.getCell(0).getStringCellValue().trim();

            String column2 = headerRow.getCell(1).getStringCellValue().trim();

            String column3 = null;

            String column4 = null;

            boolean twoColumnFormat = column1.equalsIgnoreCase("email")
                    && column2.equalsIgnoreCase("password");

            if (headerRow.getCell(2) != null) {

                column3 = headerRow.getCell(2).getStringCellValue().trim();
            }

            if (headerRow.getCell(3) != null) {

                column4 = headerRow.getCell(3).getStringCellValue().trim();
            }

            boolean fourColumnFormat = headerRow.getCell(2) != null
                            && headerRow.getCell(3) != null
                            && column1.equalsIgnoreCase("email")
                            && column2.equalsIgnoreCase("password")
                            && "firstName".equalsIgnoreCase(column3)
                            && "secondName".equalsIgnoreCase(column4);

            //checks both headers.
            if (!twoColumnFormat && !fourColumnFormat) {

                throw new BadRequestException("Invalid Excel header.");

            }

            //main loop.
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if(row == null){

                    continue;
                }

                total++;

                if (row.getCell(0) == null
                        || row.getCell(1) == null) {

                    skipped++;

                    continue;
                }

                String email = row.getCell(0)
                                .getStringCellValue()
                                .trim();

                String password = row.getCell(1)
                                .getStringCellValue()
                                .trim();

                String firstName = null;

                if (row.getCell(2) != null) {

                    firstName = row.getCell(2)
                            .getStringCellValue()
                            .trim();
                }

                String secondName =  null;

                if (row.getCell(3) != null) {

                    secondName = row.getCell(3)
                            .getStringCellValue()
                            .trim();
                }

                //checks if user is already existing.
                final User existingUser = this.userRepository.findByEmail(email)
                        .orElse(null);

                if (existingUser != null){

                    skipped++;
                    continue;
                }

                User user = User.builder()
                        .email(email)
                        .firstName(firstName)
                        .secondName(secondName)
                        .password(this.passwordEncoder.encode(password))
                        .role(userRole)
                        .organization(targetOrganization)
                        .deleted(false)
                        .mfaEnabled(false)
                        .build();

                users.add(user);

                processed++;
            }

            this.userRepository.saveAll(users);

            this.auditService.log(
                    currentUser,
                    "BULK_UPLOAD_USERS",
                    "USER",
                    null,
                    "Bulk uploaded " + processed + " users from Excel");

            return BulkUploadResponseDto.builder()
                    .totalRecords(total)
                    .processedRecords(processed)
                    .skippedRecords(skipped)
                    .build();
        }
        catch (BadRequestException e) {

            throw e;
        }

        catch (Exception e) {

            throw new BadRequestException("Invalid Excel file.");
        }
    }
}
