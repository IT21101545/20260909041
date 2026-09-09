package com.example.trainingmanagement.config;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.trainingmanagement.entity.Department;
import com.example.trainingmanagement.entity.EligibilityRule;
import com.example.trainingmanagement.entity.EligibilityRuleType;
import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.repository.DepartmentRepository;
import com.example.trainingmanagement.repository.EligibilityRuleRepository;
import com.example.trainingmanagement.repository.OfficerRepository;
import com.example.trainingmanagement.repository.TrainingRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(
            DepartmentRepository departmentRepository,
            OfficerRepository officerRepository,
            TrainingRepository trainingRepository,
                        EligibilityRuleRepository eligibilityRuleRepository,
                        JdbcTemplate jdbcTemplate) {

        return args -> {
                        removeObsoleteRuleTypeConstraints(jdbcTemplate);

            Department finance = getOrCreateDepartment(departmentRepository, "Finance Division");
            Department budget = getOrCreateDepartment(departmentRepository, "Budget Division");
            Department planning = getOrCreateDepartment(departmentRepository, "Planning Division");
            Department admin = getOrCreateDepartment(departmentRepository, "Administration Division");
            Department hr = getOrCreateDepartment(departmentRepository, "Human Resources Division");
            Department it = getOrCreateDepartment(departmentRepository, "IT Division");
            Department itSupport = getOrCreateDepartment(departmentRepository, "IT Support Division");

            if (officerRepository.count() == 0) {
                officerRepository.save(new Officer("John Perera", "john@example.com", finance,
                        "Analyst", 3));
                officerRepository.save(new Officer("Sarah Fernando", "sarah@example.com", admin,
                        "Manager", 6));
                officerRepository.save(new Officer("Kamal Silva", "kamal@example.com", hr,
                        "Officer", 1));
                officerRepository.save(new Officer("Nimal Perera", "nimal@example.com", it,
                        "Developer", 4));
                officerRepository.save(new Officer("Dinesh Kumar", "dinesh@example.com", planning,
                        "Planner", 5));
            }

            Training javaTraining = getOrCreateTraining(trainingRepository, "Java Programming",
                    "Training Hall A", 50, 10);
            Training springTraining = getOrCreateTraining(trainingRepository, "Spring Boot Development",
                    "Training Hall B", 40, 20);
            Training testingTraining = getOrCreateTraining(trainingRepository, "Software Testing",
                    "Computer Lab 1", 60, 30);
            Training financialTraining = getOrCreateTraining(trainingRepository, "Financial Management Programme",
                    "Training Hall C", 30, 40);
            Training technicalTraining = getOrCreateTraining(trainingRepository, "Technical Programme",
                    "Computer Lab 2", 30, 50);
            Training managementTraining = getOrCreateTraining(trainingRepository, "Management Development Programme",
                    "Training Hall D", 25, 60);

            addRule(eligibilityRuleRepository, javaTraining, EligibilityRuleType.ALLOWED_DEPARTMENT, finance.getName());
            addRule(eligibilityRuleRepository, springTraining, EligibilityRuleType.ALLOWED_DEPARTMENT, it.getName());
            addRule(eligibilityRuleRepository, testingTraining, EligibilityRuleType.MIN_YEARS_OF_SERVICE, "2");
            addRule(eligibilityRuleRepository, financialTraining, EligibilityRuleType.ALLOWED_DEPARTMENT, finance.getName());
            addRule(eligibilityRuleRepository, financialTraining, EligibilityRuleType.ALLOWED_DEPARTMENT, budget.getName());
            addRule(eligibilityRuleRepository, financialTraining, EligibilityRuleType.ALLOWED_DEPARTMENT, planning.getName());
            addRule(eligibilityRuleRepository, technicalTraining, EligibilityRuleType.ALLOWED_DEPARTMENT, it.getName());
            addRule(eligibilityRuleRepository, technicalTraining, EligibilityRuleType.ALLOWED_DEPARTMENT, itSupport.getName());
            addRule(eligibilityRuleRepository, managementTraining, EligibilityRuleType.ALLOWED_DESIGNATION, "Manager");
            addRule(eligibilityRuleRepository, managementTraining, EligibilityRuleType.MIN_YEARS_OF_SERVICE, "5");
            addRule(eligibilityRuleRepository, managementTraining, EligibilityRuleType.NO_RECENT_PARTICIPATION_MONTHS, "12");
        };
    }

    private Department getOrCreateDepartment(DepartmentRepository repository, String name) {
        return repository.findByName(name).orElseGet(() -> repository.save(new Department(name)));
    }

    private Training getOrCreateTraining(TrainingRepository repository, String title,
                                         String venue, int capacity, int daysFromNow) {
        return repository.findByTitle(title).orElseGet(() -> repository.save(new Training(
                title, LocalDate.now().plusDays(daysFromNow), venue, capacity)));
    }

    private void addRule(EligibilityRuleRepository repository, Training training,
                         EligibilityRuleType ruleType, String value) {
        if (!repository.existsByTrainingIdAndRuleTypeAndRuleValue(training.getId(), ruleType, value)) {
            repository.save(new EligibilityRule(training, ruleType, value));
        }
    }

        private void removeObsoleteRuleTypeConstraints(JdbcTemplate jdbcTemplate) {
                try {
                        List<String> constraintNames = jdbcTemplate.queryForList("""
                                        select cc.name
                                        from sys.check_constraints cc
                                        join sys.tables t on t.object_id = cc.parent_object_id
                                        where t.name = 'eligibility_rules'
                                          and cc.definition like '%rule_type%'
                                        """, String.class);

                        for (String constraintName : constraintNames) {
                                String safeName = constraintName.replace("]", "]]" );
                                jdbcTemplate.execute("alter table [eligibility_rules] drop constraint ["
                                                + safeName + "]");
                        }
                } catch (DataAccessException ignored) {
                }
        }
}
