package model;

public enum PropertyColor {
    DARK_BLUE(2, 3, 8),
    ORANGE(3, 1, 3, 5),
    BLACK(4, 1, 2, 3, 4),
    RED(3, 2, 3, 6),
    DARK_GREEN(3, 2, 4, 7),
    BROWN(2, 1, 2),
    PINK(3, 1, 2, 3),
    LIGHT_BLUE(3, 1, 2, 3),
    LIGHT_GREEN(2, 1, 2),
    YELLOW(3, 2, 4, 6);

    private final int amountToCompleteSet;
    private final int[] rents;

    PropertyColor(int amountToCompleteSet, int... rents) {
        this.amountToCompleteSet = amountToCompleteSet;
        this.rents = rents;
    }

    public int getAmountToCompleteSet() {
        return amountToCompleteSet;
    }

    // Finds rent for property count.
    public int getRentForPropertyCount(int propertyCount) {
        return rents[propertyCount - 1];
    }

    // Finds short name.
    public String getShortName() {
        String displayName = getDisplayName();

        if (this == DARK_BLUE) {
            return "D.BLUE";
        }

        if (this == DARK_GREEN) {
            return "D.GREEN";
        }

        if (this == LIGHT_BLUE) {
            return "L.BLUE";
        }

        if (this == LIGHT_GREEN) {
            return "L.GREEN";
        }

        return displayName.toUpperCase();
    }

    // Finds display name.
    public String getDisplayName() {
        String[] words = name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (!builder.isEmpty()) {
                builder.append(" ");
            }

            builder.append(word.substring(0, 1).toUpperCase());
            builder.append(word.substring(1));
        }

        return builder.toString();
    }
}
