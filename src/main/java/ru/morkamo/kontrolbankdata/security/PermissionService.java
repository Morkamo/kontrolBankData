package ru.morkamo.kontrolbankdata.security;

import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private static final Set<String> SPECIALIST_FIELDS = Set.of("ovidSpecialist", "ovidNote");
    private static final Set<String> DB_MANAGEMENT_FIELDS = Set.of(
            "ovidSpecialist",
            "ovidNote",
            "executionMark");
    private static final Set<String> MANUAL_CONTROL_FIELDS = Set.of(
            "ovidSpecialist",
            "ovidNote",
            "controlResult1",
            "controlSpecialist1",
            "controlResult2",
            "controlSpecialist2");

    public boolean canDeleteRecords(Integer departmentId) {
        return isControl(departmentId) || isAdministrator(departmentId);
    }

    public boolean isAdministrator(Integer departmentId) {
        return departmentId != null && departmentId == DepartmentIds.ADMINISTRATOR;
    }

    public boolean canEditAnyRecord(Integer departmentId) {
        return isControl(departmentId);
    }

    public boolean canEditField(Integer departmentId, JournalType journal, String field) {
        return field != null && editableFields(departmentId, journal).contains(field);
    }

    public boolean canEditSomeFields(Integer departmentId, JournalType journal) {
        return !editableFields(departmentId, journal).isEmpty();
    }

    public boolean canOpenEdit(Integer departmentId, JournalType journal) {
        return canEditAnyRecord(departmentId) || canEditSomeFields(departmentId, journal);
    }

    public Set<String> editableFields(Integer departmentId, JournalType journal) {
        if (departmentId == null || journal == null) {
            return Set.of();
        }

        if (isControl(departmentId) || isAdministrator(departmentId)) {
            return journal == JournalType.MANUAL ? MANUAL_CONTROL_FIELDS : SPECIALIST_FIELDS;
        }

        if (departmentId == DepartmentIds.DB_MANAGEMENT) {
            return DB_MANAGEMENT_FIELDS;
        }

        return (isBilling(departmentId) || isAdministrator(departmentId)) ? SPECIALIST_FIELDS : Set.of();
    }

    private boolean isControl(Integer departmentId) {
        return departmentId != null && DepartmentIds.CONTROL == departmentId;
    }

    private boolean isBilling(Integer departmentId) {
        return departmentId == DepartmentIds.BILLINGS_1 || departmentId == DepartmentIds.BILLINGS_2;
    }
}
