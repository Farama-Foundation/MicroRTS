package rts;

public class VariableSpec {
    public static final int TYPE_CATEGORICAL = 0;
    public static final int TYPE_CONTINUOUS = 1;
    String name = "";
    int type = 0;
    int num_categories = 0;

    public VariableSpec(String name, int type, int num_categories) {
        this.name = name;
        this.type = type;
        this.num_categories = num_categories;
    }
}