package ru.morkamo.kontrolbankdata.security;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private static final Set<String> SPECIALIST_FIELDS = Set.of("ovidSpecialist", "ovidNote");
    private static final Set<String> DB_MANAGEMENT_FIELDS = Set.of(
            "ovidSpecialist",
            "ovidNote",
            "executionMark");
    private static final Map<JournalType, Set<String>> LIMITED_DEPARTMENT_FIELDS = Map.of(
            JournalType.BANK, SPECIALIST_FIELDS,
            JournalType.SED, SPECIALIST_FIELDS,
            JournalType.MANUAL, SPECIALIST_FIELDS);

    private static final Map<JournalType, Set<String>> CONTROL_FIELDS = Map.of(
            JournalType.BANK, SPECIALIST_FIELDS,
            JournalType.SED, SPECIALIST_FIELDS,
            JournalType.MANUAL, Set.of(
                    "ovidSpecialist",
                    "ovidNote",
                    "controlResult1",
                    "controlSpecialist1",
                    "controlResult2",
                    "controlSpecialist2"));

    public boolean canDeleteRecords(Integer departmentId) {
        return isControl(departmentId);
    }

    public boolean canEditAnyRecord(Integer departmentId) {
        return isControl(departmentId);
    }

    public boolean canEditField(Integer departmentId, JournalType journal, String field) {
        if (departmentId == null || journal == null || field == null) {
            return false;
        }

        if (isControl(departmentId)) {
            return CONTROL_FIELDS.getOrDefault(journal, Set.of()).contains(field);
        }

        return limitedDepartmentFields(departmentId, journal).contains(field);
    }

    public boolean canEditSomeFields(Integer departmentId, JournalType journal) {
        return !editableFields(departmentId, journal).isEmpty();
    }

    public Set<String> editableFields(Integer departmentId, JournalType journal) {
        if (departmentId == null || journal == null) {
            return Set.of();
        }

        if (isControl(departmentId)) {
            return CONTROL_FIELDS.getOrDefault(journal, Set.of());
        }

        Set<String> limitedDepartmentFields = limitedDepartmentFields(departmentId, journal);
        if (!limitedDepartmentFields.isEmpty()) {
            return limitedDepartmentFields;
        }

        return Set.of();
    }

    private Set<String> limitedDepartmentFields(Integer departmentId, JournalType journal) {
        if (departmentId == null || journal == null || !isLimitedDepartment(departmentId)) {
            return Set.of();
        }

        if (departmentId == DepartmentIds.DB_MANAGEMENT) {
            return DB_MANAGEMENT_FIELDS;
        }

        return LIMITED_DEPARTMENT_FIELDS.getOrDefault(journal, Set.of());
    }

    private boolean isControl(Integer departmentId) {
        return departmentId != null && DepartmentIds.CONTROL == departmentId;
    }

    private boolean isLimitedDepartment(Integer departmentId) {
        return departmentId != null
                && (departmentId == DepartmentIds.DB_MANAGEMENT
                || departmentId == DepartmentIds.BILLINGS_1
                || departmentId == DepartmentIds.BILLINGS_2);
    }
}
