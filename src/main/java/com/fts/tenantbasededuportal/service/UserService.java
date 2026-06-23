package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dtos.user.*;
import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.exception.ResourceNotFoundException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.OrganizationRepository;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.fts.tenantbasededuportal.util.RoleConstants;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    //Performs a GET operation and fetches all users from the DB.
    public List<UserResponseDto> fetchUsers(){

        final User currentUser = this.securityUtil.getCurrentUser();

        final String roleName = currentUser.getRole().getName();

        final List<User> users;

        //checks if the logged-in user is super admin if so allows the operation.
        if(RoleConstants.SUPER_ADMIN.equals(roleName)){

            users = this.userRepository.findAll();
        }
        //checks if the logged-in user is org admin if so allows the operation.
        else if (RoleConstants.ORG_ADMIN.equals(roleName)) {

            users = this.userRepository.findByOrganization(
                    currentUser.getOrganization());
        }
        //deny the operation for users.
        else {
            throw new UnauthorizedException(
                    "you don't have permission to view users");
        }

        final List<UserResponseDto> response = new ArrayList<>();

        for(final User user : users){

            String organizationName = null;

            if (user.getOrganization() != null){

                organizationName = user.getOrganization().getName();
            }

            response.add(
                    UserResponseDto.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .roleName(user.getRole().getName())
                            .mfaEnabled(user.getMfaEnabled())
                            .deleted(user.getDeleted())
                            .organizationName(organizationName)
                            .createdAt(user.getCreatedAt())
                            .build()
            );
        }

        this.auditService.log(
                currentUser,
                "VIEW_USERS",
                "USER",
                null,
                "Viewed users list");

        return response;
    }

    //Performs a GET operation and fetches the user based on the given id.
    public UserResponseDto fetchUserById(final String id){

        final User currentUser = this.securityUtil.getCurrentUser();

        final User targetUser = this.userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "User not found."));

        final String roleName = currentUser.getRole().getName();

        //checks if the current user is org admin.
        if (RoleConstants.ORG_ADMIN.equals(roleName)) {

            /*checks the role of target user is null and compares it with
            the role of current user (both roles should match) (false and true,
             true and false or true and true gives exception).
             */
            if (targetUser.getOrganization() == null
                    || !targetUser.getOrganization().getId()
                    .equals(currentUser.getOrganization().getId())) {

                throw new UnauthorizedException(
                        "You cannot access users from another organization.");
            }
        }

        //if role is org admin then they can only view users.
        if (RoleConstants.ORG_ADMIN.equals(roleName)
                && !RoleConstants.USER.equals(targetUser.getRole().getName())) {

            throw new UnauthorizedException
                    ("Organization admins can only view users.");
        }

        String organizationName = null;

        if (targetUser.getOrganization() != null) {

            organizationName = targetUser.getOrganization().getName();
        }

        this.auditService.log(
                currentUser,
                "VIEW_USER",
                "USER",
                targetUser.getId(),
                "Viewed user " + targetUser.getEmail()
        );

        return UserResponseDto.builder()
                .id(targetUser.getId())
                .email(targetUser.getEmail())
                .roleName(targetUser.getRole().getName())
                .organizationName(organizationName)
                .mfaEnabled(targetUser.getMfaEnabled())
                .deleted(targetUser.getDeleted())
                .createdAt(targetUser.getCreatedAt())
                .build();
    }

    //Performs a POST operation and creates a user in DB.
    public UserResponseDto createUser(final CreateUserRequestDto request){

        final User currentUser = this.securityUtil.getCurrentUser();

        //checks if the user already exists with email.
        if(this.userRepository.existsByEmail(request.getEmail())){
            throw new BadRequestException("Email already exists");
        }

        final String currentRole = currentUser.getRole().getName();

        final String requestedRole = request.getRoleName();

        final Role role;

        final Organization organization;

        //checks if current user is super admin.
        if(RoleConstants.SUPER_ADMIN.equals(currentRole)){

            //can only create user and org admin.
            if(!RoleConstants.USER.equals(requestedRole)
            && !RoleConstants.ORG_ADMIN.equals(requestedRole)){

                throw new BadRequestException("Invalid role.");
            }

            //only can create an org admin with an organization id.
            if (RoleConstants.ORG_ADMIN.equals(requestedRole)
                    && request.getOrganizationId() == null) {

                throw new BadRequestException(
                        "Organization admin must belong to an organization.");
            }

            role = this.roleRepository.findByName(requestedRole)
                    .orElseThrow(()->new ResourceNotFoundException(
                            "Role not found"));

            //if request has org id sets it to a variable.
            if (request.getOrganizationId() != null){

                organization = this.organizationRepository
                        .findById(request.getOrganizationId())
                        .orElseThrow(()->new ResourceNotFoundException(
                                "Organization not found"));
            }
            else {
                organization = null;
            }
        }
        //checks if current user is org admin
        else if (RoleConstants.ORG_ADMIN.equals(currentRole)) {

            //if current user is org admin then can only create users in own org.
            if(!RoleConstants.USER.equals(requestedRole)){

                throw new UnauthorizedException(
                        "Organization admins can only create users");
            }

            role = this.roleRepository.findByName(RoleConstants.USER)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Role not found"));

            organization = currentUser.getOrganization();
        }
        else {

            throw new UnauthorizedException(
                    "You do not have permission to create users.");
        }

        final User user = User.builder()
                .email(request.getEmail())
                .password(this.passwordEncoder.encode(
                        request.getPassword()))
                .firstName(request.getFirstName())
                .secondName(request.getSecondName())
                .role(role)
                .organization(organization)
                .deleted(false)
                .mfaEnabled(false)
                .build();

        this.userRepository.save(user);

        String organizationName = null;

        if (organization != null){

            organizationName = organization.getName();

        }

        this.auditService.log(
                currentUser,
                "CREATE_USER",
                "USER",
                user.getId(),
                "Created user " + user.getEmail());

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roleName(role.getName())
                .organizationName(organizationName)
                .mfaEnabled(user.getMfaEnabled())
                .deleted(user.getDeleted())
                .createdAt(user.getCreatedAt())
                .build();
    }

    //performs a PUT operation and updates user with the help of id.
    //both super admin and org admin can access.
    public UserResponseDto updateUser(final String id,
            final UpdateUserRequestDto request) {

        final User currentUser = this.securityUtil.getCurrentUser();

        final User targetUser = this.userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "User not found."));

        final String currentRole = currentUser.getRole().getName();

        //check is current role is org admin.
        if (RoleConstants.ORG_ADMIN.equals(currentRole)) {

            /*checks both organization of current and target user are same
            if target organization is not null.
             */
            if (targetUser.getOrganization() == null
                    || !targetUser.getOrganization()
                    .getId()
                    .equals(currentUser.getOrganization().getId())) {

                throw new UnauthorizedException(
                        "You cannot update users from another organization."
                );
            }
        }

        //prevents org admins from updating other org admins.
        if (RoleConstants.ORG_ADMIN.equals(currentRole)
                && !RoleConstants.USER.equals(targetUser.getRole().getName())) {

            throw new UnauthorizedException
                    ("Organization admins can only update users.");
        }

        if (request.getEmail() != null) {

            //checks if the email we are updating already exists if so throws error.
            if (!targetUser.getEmail().equals(request.getEmail())
                    && this.userRepository.existsByEmail(
                    request.getEmail())) {

                throw new BadRequestException(
                        "Email already exists."
                );
            }

            targetUser.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            targetUser.setFirstName(request.getFirstName());
        }

        if (request.getSecondName() != null) {
            targetUser.setSecondName(request.getSecondName());
        }

        //checks current user is super admin
        if (RoleConstants.SUPER_ADMIN.equals(currentRole)
                && request.getRoleName() != null) {

            //can only update users and org_admin.
            if (!RoleConstants.USER.equals(request.getRoleName())
                    && !RoleConstants.ORG_ADMIN.equals(request.getRoleName())) {

                throw new BadRequestException("Invalid role.");
            }

            //makes sure if updating org admin organization is present
            if (RoleConstants.ORG_ADMIN.equals(request.getRoleName())
                    && request.getOrganizationId() == null
                    && targetUser.getOrganization() == null) {

                throw new BadRequestException(
                        "Organization admin must belong to an organization."
                );
            }

            final Role role = this.roleRepository.findByName(
                                    request.getRoleName())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Role not found."));

            targetUser.setRole(role);
        }

        //checks if super admin and organization in request is not null.
        if (RoleConstants.SUPER_ADMIN.equals(currentRole)
                && request.getOrganizationId() != null) {

            final Organization organization = this.organizationRepository
                            .findById(request.getOrganizationId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Organization not found."));

            targetUser.setOrganization(organization);
        }

        this.userRepository.save(targetUser);

        String organizationName = null;

        if (targetUser.getOrganization() != null) {

            organizationName = targetUser.getOrganization().getName();
        }

        this.auditService.log(
                currentUser,
                "UPDATE_USER",
                "USER",
                targetUser.getId(),
                "Updated user " + targetUser.getEmail());

        return UserResponseDto.builder()
                .id(targetUser.getId())
                .email(targetUser.getEmail())
                .firstName(targetUser.getFirstName())
                .secondName(targetUser.getSecondName())
                .roleName(targetUser.getRole().getName())
                .organizationName(organizationName)
                .mfaEnabled(targetUser.getMfaEnabled())
                .deleted(targetUser.getDeleted())
                .createdAt(targetUser.getCreatedAt())
                .build();
    }


    //performs a DELETE operation and soft deletes (sets deleted to true in user table).
    public void deleteUser(final String id) {

        final User currentUser = this.securityUtil.getCurrentUser();

        final User targetUser = this.userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "User not found."));

        final String currentRole = currentUser.getRole().getName();

        //checks if logged-in user is org admin.
        if (RoleConstants.ORG_ADMIN.equals(currentRole)) {

            /*if org admin then checks that org of target is not null
            and belongs to the same org as org admin(current user)
             */
            if (targetUser.getOrganization() == null
                    || !targetUser.getOrganization().getId().equals(
                            currentUser.getOrganization().getId())) {

                throw new UnauthorizedException(
                        "You cannot delete users from another organization.");
            }
        }

        //prevents org admin from deleting any other roles other than users.
        if (RoleConstants.ORG_ADMIN.equals(currentRole)
                && !RoleConstants.USER.equals(targetUser.getRole().getName())) {

            throw new UnauthorizedException(
                    "Organization admins can only delete users.");
        }

        if (Boolean.TRUE.equals(targetUser.getDeleted())) {

            throw new BadRequestException(
                    "User is already deleted."
            );
        }

        //if not already deleted sets the target deleted to true (soft delete).
        targetUser.setDeleted(true);

        this.userRepository.save(targetUser);

        this.auditService.log(
                currentUser,
                "DELETE_USER",
                "USER",
                targetUser.getId(),
                "Soft deleted user " + targetUser.getEmail());
    }

    //performs a PUT operation and bulk uploads users to DB.
    //only org admins can bulk upload.
    public BulkUploadResponseDto bulkUploadUsers(
            final MultipartFile file){

        final User currentUser = this.securityUtil.getCurrentUser();

        //checks if current user is org admin.
        if (!RoleConstants.ORG_ADMIN.equals(
                currentUser.getRole().getName())){

            throw new UnauthorizedException(
                    "Only org_admins can bulk upload users.");
        }

        final String fileName = file.getOriginalFilename();

        if (fileName == null) {

            throw new BadRequestException(
                    "File name is missing.");
        }

        if (fileName.endsWith(".csv")){

            return uploadCsv(file, currentUser, false, null);
        }

        if (fileName.endsWith(".xlsx")){

            return uploadExcel(file, currentUser, false, null);
        }

        throw new BadRequestException(
                "Only CSV and XLSX files are supported.");
    }

    //method for uploading/restoring csv called by bulk upload and restore.
    //takes file, current user, a boolean restore and organization id as parameters based on the operation.
    private BulkUploadResponseDto uploadCsv
            (final MultipartFile file, final User currentUser,
             final boolean restore, final Organization restoreOrganization ) {

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

            Role userRole = null;

            /*checks if we are restoring and if we are then sets role to user.
            can only bulk restore users in an org.
             */
            if(!restore){

                userRole = this.roleRepository.findByName(
                        RoleConstants.USER).orElseThrow(() ->
                        new ResourceNotFoundException("Role not found."));
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

                //runs if we are bulk restoring
                if(restore){

                    if (existingUser == null){

                        skipped++;
                        continue;
                    }

                    if (!existingUser.getDeleted()){

                        skipped++;
                        continue;
                    }

                    //restores (sets existing user false)
                    existingUser.setDeleted(false);

                    existingUser.setOrganization(restoreOrganization);

                    this.userRepository.save(existingUser);

                    processed++;
                    continue;
                }

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
                        .organization(currentUser.getOrganization())
                        .deleted(false)
                        .mfaEnabled(false)
                        .build();

                users.add(user);

                processed++;
            }

            if (!restore){

                this.userRepository.saveAll(users);
            }

            if (restore){

                this.auditService.log(
                        currentUser,
                        "BULK_RESTORE_USERS",
                        "USER",
                        restoreOrganization.getId(),
                        "Bulk restored " + processed + " users from CSV.");
            }
            else{

                this.auditService.log(
                        currentUser,
                        "BULK_UPLOAD_USERS",
                        "USER",
                        null,
                        "Bulk uploaded " + processed + " users from CSV");
            }

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
             final boolean restore, final Organization restoreOrganization) {

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

            Role userRole = null;

            /*checks if we are restoring and if we are then sets role to user.
            can only bulk restore users in an org.
             */
            if(!restore){

                userRole = this.roleRepository.findByName(
                        RoleConstants.USER).orElseThrow(()->
                        new ResourceNotFoundException("Role not found."));
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

                //runs if restoring.
                if(restore){

                    if (existingUser == null){

                        skipped++;
                        continue;
                    }

                    //skips if not deleted
                    if (!existingUser.getDeleted()){

                        skipped++;
                        continue;
                    }

                    existingUser.setDeleted(false);

                    existingUser.setOrganization(restoreOrganization);

                    this.userRepository.save(existingUser);

                    processed++;
                    continue;
                }

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
                        .organization(currentUser.getOrganization())
                        .deleted(false)
                        .mfaEnabled(false)
                        .build();

                users.add(user);

                processed++;
            }

            if (!restore){

                this.userRepository.saveAll(users);
            }

            if (restore){

                this.auditService.log(
                        currentUser,
                        "BULK_RESTORE_USERS",
                        "USER",
                        restoreOrganization.getId(),
                        "Bulk restored " + processed + " users from Excel.");
            }
            else{

                this.auditService.log(
                        currentUser,
                        "BULK_UPLOAD_USERS",
                        "USER",
                        null,
                        "Bulk uploaded " + processed + " users from Excel");
            }

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

    //performs a DELETE operation that deletes all users in an org.
    //can only be done by super admin.
    public void deleteUsersByOrganization(final String organizationId) {

        final User currentUser = this.securityUtil.getCurrentUser();

        //checks if user is super admin
        if (!RoleConstants.SUPER_ADMIN.equals(currentUser.getRole().getName())){

            throw new UnauthorizedException("Only super admin can delete users by organization");
        }

        final Organization organization = this.organizationRepository
                        .findById(organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException
                                ("Organization not found."));

        final List<User> users = this.userRepository
                        .findByOrganization(organization);

        //loops through org users of given org id and sets deleted and org as true and null.
        for (User user : users) {

            user.setDeleted(true);

            user.setOrganization(null);
        }

        this.userRepository.saveAll(users);

        this.auditService.log(
                currentUser,
                "SOFT_DELETE_USERS_BY_ORGANIZATION",
                "ORGANIZATION",
                organizationId,
                "Soft deleted "
                        + users.size() + " users from organization "
                        + organization.getName());
    }


    //performs a PUT operation and restores all soft deleted users.
    //org id is needed for restoring a deleted user to a new org.
    //can br done by super and org admin.
    public UserResponseDto restoreUser(final String id,
                                       final RestoreUserRequestDto request) {

        final  User currentUser = this.securityUtil.getCurrentUser();

        final String currentRole = currentUser.getRole().getName();

        final User targetUser = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!targetUser.getDeleted()){

            throw new BadRequestException("User already active.");
        }

        //check for org admin.
        if (RoleConstants.ORG_ADMIN.equals(currentRole)){

            //checks if org admin only restores users of their org.
            if (!RoleConstants.USER.equals(targetUser.getRole().getName())){

                throw new UnauthorizedException("Organization Admins can only restore users.");
            }

            if(targetUser.getOrganization() == null || !targetUser.getOrganization()
                    .getId().equals(currentUser.getOrganization().getId())){

                throw new UnauthorizedException
                        ("you can only restore users in your own organization");
            }
        }

        if (request.getOrganizationId() != null){

            //checks if org id is given and role is org admin then org admin can only restore users to their org.
            if (RoleConstants.ORG_ADMIN.equals(currentRole)
                    && !request.getOrganizationId()
                    .equals(currentUser.getOrganization().getId())) {

                throw new UnauthorizedException
                        ("Organization admins can only assign users to their own organization.");
            }

            final Organization organization = this.organizationRepository
                    .findById(request.getOrganizationId())
                    .orElseThrow(()-> new ResourceNotFoundException
                            ("Organization not found"));

            targetUser.setOrganization(organization);
        }

        targetUser.setDeleted(false);

        this.userRepository.save(targetUser);

        this.auditService.log(
                currentUser,
                "RESTORE_USER",
                "USER",
                targetUser.getId(),
                "Restored user: " + targetUser.getEmail());

        String organizationName = null;

        if (targetUser.getOrganization() != null){

            organizationName = targetUser.getOrganization().getName();
        }

        return UserResponseDto.builder()
                .id(targetUser.getId())
                .email(targetUser.getEmail())
                .firstName(targetUser.getFirstName())
                .secondName(targetUser.getSecondName())
                .roleName(targetUser.getRole().getName())
                .organizationName(organizationName)
                .deleted(targetUser.getDeleted())
                .mfaEnabled(targetUser.getMfaEnabled())
                .build();
    }


    //performs a POST operation and bulk restores users with a file.
    //can be done by org admin and super admin.
    public BulkUploadResponseDto bulkRestoreUsers(final MultipartFile file,
                                                  final String organizationId){

        final User currentUser = this.securityUtil.getCurrentUser();

        final Organization organization = this.organizationRepository
                .findById(organizationId).orElseThrow(()->
                        new ResourceNotFoundException("Organization not found"));

        final String roleName = currentUser.getRole().getName();

        //checks if current user is user and if so block them.
        if (!RoleConstants.SUPER_ADMIN.equals(roleName)
                && !RoleConstants.ORG_ADMIN.equals(roleName)) {

            throw new UnauthorizedException("You are not authorized to bulk restore users.");
        }

        //checks if org admin then can only restore their own org users.
        if (RoleConstants.ORG_ADMIN.equals(currentUser.getRole().getName())
                && !organization.getId().equals(currentUser.getOrganization().getId())) {

            throw new UnauthorizedException(
                    "You can only restore users to your own organization.");
        }

        final String fileName = file.getOriginalFilename();

        if (fileName == null){

            throw new BadRequestException("File is missing");
        }

        if (fileName.endsWith(".csv")){

            return uploadCsv(file, currentUser, true, organization);
        }

        if (fileName.endsWith(".xlsx")){

            return uploadExcel(file, currentUser, true, organization);
        }

        throw new BadRequestException("Only CSV and XLSX files are supported");
    }
}
