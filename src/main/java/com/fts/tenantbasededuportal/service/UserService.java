package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dtos.user.BulkUploadResponseDto;
import com.fts.tenantbasededuportal.dtos.user.CreateUserRequestDto;
import com.fts.tenantbasededuportal.dtos.user.UpdateUserRequestDto;
import com.fts.tenantbasededuportal.dtos.user.UserResponseDto;
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

    public List<UserResponseDto> fetchUsers(){

        final User currentUser = this.securityUtil.getCurrentUser();

        final String roleName = currentUser.getRole().getName();

        final List<User> users;

        if(RoleConstants.SUPER_ADMIN.equals(roleName)){

            users = this.userRepository.findAll();
        }
        else if (RoleConstants.ORG_ADMIN.equals(roleName)) {

            users = this.userRepository.findByOrganization(
                    currentUser.getOrganization());
        }
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

    public UserResponseDto fetchUserById(final String id){

        final User currentUser = this.securityUtil.getCurrentUser();

        final User targetUser = this.userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "User not found."));

        final String roleName = currentUser.getRole().getName();

        if (RoleConstants.ORG_ADMIN.equals(roleName)) {

            if (targetUser.getOrganization() == null
                    || !targetUser.getOrganization().getId()
                    .equals(currentUser.getOrganization().getId())) {

                throw new UnauthorizedException(
                        "You cannot access users from another organization."
                );
            }
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

    public UserResponseDto createUser(final CreateUserRequestDto request){

        final User currentUser = this.securityUtil.getCurrentUser();

        if(this.userRepository.existsByEmail(request.getEmail())){
            throw new BadRequestException("Email already exists");
        }

        final String currentRole = currentUser.getRole().getName();

        final String requestedRole = request.getRoleName();

        final Role role;

        final Organization organization;

        if(RoleConstants.SUPER_ADMIN.equals(currentRole)){

            if(!RoleConstants.USER.equals(requestedRole)
            && !RoleConstants.ORG_ADMIN.equals(requestedRole)){

                throw new BadRequestException("Invalid role.");
            }

            if (RoleConstants.ORG_ADMIN.equals(requestedRole)
                    && request.getOrganizationId() == null) {

                throw new BadRequestException(
                        "Organization admin must belong to an organization.");
            }

            role = this.roleRepository.findByName(requestedRole)
                    .orElseThrow(()->new ResourceNotFoundException(
                            "Role not found"));

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
        else if (RoleConstants.ORG_ADMIN.equals(currentRole)) {

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

    public UserResponseDto updateUser(final String id,
            final UpdateUserRequestDto request) {

        final User currentUser = this.securityUtil.getCurrentUser();

        final User targetUser = this.userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "User not found."));

        final String currentRole = currentUser.getRole().getName();

        if (RoleConstants.ORG_ADMIN.equals(currentRole)) {

            if (targetUser.getOrganization() == null
                    || !targetUser.getOrganization()
                    .getId()
                    .equals(currentUser.getOrganization().getId())) {

                throw new UnauthorizedException(
                        "You cannot update users from another organization."
                );
            }
        }

        if (request.getEmail() != null) {

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

        if (RoleConstants.SUPER_ADMIN.equals(currentRole)
                && request.getRoleName() != null) {

            if (!RoleConstants.USER.equals(request.getRoleName())
                    && !RoleConstants.ORG_ADMIN.equals(request.getRoleName())) {

                throw new BadRequestException("Invalid role.");
            }

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

    public void deleteUser(final String id) {

        final User currentUser = this.securityUtil.getCurrentUser();

        final User targetUser = this.userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "User not found."));

        final String currentRole = currentUser.getRole().getName();

        if (RoleConstants.ORG_ADMIN.equals(currentRole)) {

            if (targetUser.getOrganization() == null
                    || !targetUser.getOrganization().getId().equals(
                            currentUser.getOrganization().getId())) {

                throw new UnauthorizedException(
                        "You cannot delete users from another organization."
                );
            }
        }

        if (Boolean.TRUE.equals(targetUser.getDeleted())) {

            throw new BadRequestException(
                    "User is already deleted."
            );
        }

        targetUser.setDeleted(true);

        this.userRepository.save(targetUser);

        this.auditService.log(
                currentUser,
                "DELETE_USER",
                "USER",
                targetUser.getId(),
                "Soft deleted user " + targetUser.getEmail());
    }

    public BulkUploadResponseDto bulkUploadUsers(
            final MultipartFile file){

        final User currentUser = this.securityUtil.getCurrentUser();

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

            return uploadCsv(file, currentUser);
        }

        if (fileName.endsWith(".xlsx")){

            return uploadExcel(file, currentUser);
        }

        throw new BadRequestException(
                "Only CSV and XLSX files are supported.");
    }

    private BulkUploadResponseDto uploadCsv
            (final MultipartFile file, final User currentUser) {

        final List<User> users = new ArrayList<>();

        int total = 0;
        int created = 0;
        int skipped = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))){

            String line;
            String header = br.readLine();

            if (header == null) {

                throw new BadRequestException("CSV file is empty.");
            }

            header = header.trim();

            if (!header.equalsIgnoreCase("email,password")
            && !header.equalsIgnoreCase("email,password,firstName,secondName")) {

                throw new BadRequestException("Invalid CSV header.");
            }

            final Role userRole = this.roleRepository.findByName(
                            RoleConstants.USER).orElseThrow(() ->
                            new ResourceNotFoundException("Role not found."));

            while ((line = br.readLine()) != null){

                total++;

                String[] values = line.split(",");

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

                if (this.userRepository.existsByEmail(email)){

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

                created++;
            }

            this.userRepository.saveAll(users);

            this.auditService.log(
                    currentUser,
                    "BULK_UPLOAD_USERS",
                    "USER",
                    null,
                    "Bulk uploaded " + created + " users from CSV");

            return BulkUploadResponseDto.builder()
                    .totalRecords(total)
                    .createdRecords(created)
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

    private BulkUploadResponseDto uploadExcel
            (final MultipartFile file, final User currentUser) {

        final List<User> users = new ArrayList<>();

        int total = 0;
        int created = 0;
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

            if (!twoColumnFormat && !fourColumnFormat) {

                throw new BadRequestException("Invalid Excel header.");

            }

            final Role userRole = this.roleRepository.findByName(
                    RoleConstants.USER).orElseThrow(()->
                    new ResourceNotFoundException("Role not found."));

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

                if (this.userRepository.existsByEmail(email)){

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

                created++;
            }

            this.userRepository.saveAll(users);

            this.auditService.log(
                    currentUser,
                    "BULK_UPLOAD_USERS",
                    "USER",
                    null,
                    "Bulk uploaded " + created + " users from Excel");

            return BulkUploadResponseDto.builder()
                    .totalRecords(total)
                    .createdRecords(created)
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

    public void deleteUsersByOrganization(final String organizationId) {

        final User currentUser = this.securityUtil.getCurrentUser();

        final Organization organization = this.organizationRepository
                        .findById(organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException
                                ("Organization not found."));

        final List<User> users = this.userRepository
                        .findByOrganization(organization);

        for (User user : users) {

            user.setDeleted(true);
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
}
