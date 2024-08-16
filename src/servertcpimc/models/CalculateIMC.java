package servertcpimc.models;

import java.io.Serializable;

public class CalculateIMC implements Serializable{
    private float weight;
    private float height;
    private Imc imc;
    
    public static class Imc {
        public float result;
        public String message;
    }

    public CalculateIMC() {
    }

    public CalculateIMC(float weight, float height) {
        this.weight = weight;
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public Imc getImc() {
        imc = new Imc();
        if(weight <= 0 || height <= 0){
            imc.message = "ERROR: El peso y la altura deben ser mayores a 0.";
            return imc;
        } else {
            imc.result = weight / (height * height);
            if(imc.result < 18.5){
                imc.message = "Deberias visitar un medico, tu peso es muy bajo.";
            } else if (imc.result >= 18.5 && imc.result <= 24.9){
                imc.message = "Estas bien de peso.";
            } else if (imc.result > 24.9 && imc.result <= 29.9){
                imc.message = "Deberias bajar un poco de peso.";
            } else{
                imc.message = "Deberias visitar un medico, tu peso es muy alto.";
            }
            return imc;
        }
    }

}
