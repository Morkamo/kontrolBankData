package ru.morkamo.kontrolbankdata.constants;

import java.util.Set;

public final class RecordValues {

    public static final Set<String> ALLOWED_USAGE_MARKS = Set.of(
            "Исполнено", "Без исполнения", "Уведомление", "Отзыв");
    public static final Set<String> ALLOWED_EXECUTION_MARKS = Set.of("Да", "Нет");
    public static final Set<String> ALLOWED_URGENCY_VALUES = Set.of("Да", "Нет");
    public static final Set<String> ALLOWED_BANK_RECALL_TYPES = Set.of("Полный", "Частичный");
    public static final Set<String> ALLOWED_BANK_AND_SED_REASONS = Set.of(
            "Умер", "Выбыл", "Утрата права", "Смена доставочной организации");
    public static final Set<String> ALLOWED_MANUAL_REASONS = Set.of(
            "возобновление из приостановленных",
            "возобновление после приостановления доставочного документа",
            "корректировка",
            "оформление");
    public static final Set<String> ALLOWED_ACCOUNTING_VALUES = Set.of("Текущий", "Предыдущий");

    private RecordValues() {
    }
}
