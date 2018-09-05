package fields.recognition;

public class ClassificationResults {
    private String name;
    private float probability;

    public ClassificationResults(String name, float propability) {
        this.name = name;
        this.probability = propability;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }
}
