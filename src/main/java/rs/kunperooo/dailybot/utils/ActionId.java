package rs.kunperooo.dailybot.utils;

public enum ActionId {
    START_CHECK_IN, OTHER;

    public static ActionId safeValueOf(String value) {
        try {
            return ActionId.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return OTHER;
        }
    }
}
