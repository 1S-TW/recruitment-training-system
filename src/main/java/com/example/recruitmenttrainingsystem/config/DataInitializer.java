package com.example.recruitmenttrainingsystem.config;

import com.example.recruitmenttrainingsystem.entity.Department;
import com.example.recruitmenttrainingsystem.entity.Role;
import com.example.recruitmenttrainingsystem.repository.DepartmentRepository;
import com.example.recruitmenttrainingsystem.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;

    public DataInitializer(RoleRepository roleRepository, DepartmentRepository departmentRepository) {
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            // SỬA: Dùng constructor mặc định + setter
            Role superAdmin = new Role();
            superAdmin.setRoleName("SUPER_ADMIN");
            superAdmin.setDescription("Full quyền");
            roleRepository.save(superAdmin);

            Role deptHead = new Role();
            deptHead.setRoleName("DEPARTMENT_HEAD");
            deptHead.setDescription("Trưởng bộ phận/nhóm");
            roleRepository.save(deptHead);

            Role trainingManager = new Role();
            trainingManager.setRoleName("TRAINING_MANAGER");
            trainingManager.setDescription("QLDT - Quản lý đào tạo");
            roleRepository.save(trainingManager);

            Role qualityControl = new Role();
            qualityControl.setRoleName("QUALITY_CONTROL");
            qualityControl.setDescription("KSCL - Kiểm soát chất lượng");
            roleRepository.save(qualityControl);

            Role hr = new Role();
            hr.setRoleName("HR");
            hr.setDescription("Nhân sự");
            roleRepository.save(hr);
        }

        if (departmentRepository.count() == 0) {
            Department dept = new Department();
            dept.setDepartmentName("Default Department");
            dept.setStatus(true);
            departmentRepository.save(dept);
        }
    }
}