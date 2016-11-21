package neuralnetwork;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by David et Monireh on 16/10/2016.
 */
public class Network {

    protected List<List<Neuron>> layers = new ArrayList<>();

    /**
     *
     * @param nbNeuronByLayer the number of neurons by layer except output layer, which is implicitely equals to 1
     */
    public Network(boolean randomizeWeight, int ... nbNeuronByLayer){
        // Create the layers and the neurons on each layer
        // The first layer will be populated with InputNeurons
        Random rand = new Random();
        for (int indexLayer = 0; indexLayer < nbNeuronByLayer.length; indexLayer++) {
            layers.add(new ArrayList<>());
            List<Neuron> layer = layers.get(indexLayer);
            for (int i = 0; i < nbNeuronByLayer[indexLayer]; i++) {
                layer.add(indexLayer == 0 ? new InputNeuron() : new Neuron());
            }
        }
        // Create the output layer, made of one neuron
        ArrayList<Neuron> output = new ArrayList<>();
        output.add(new Neuron());
        layers.add(output);

        // Link neurons to the previous layer backwards
        for (int indexLayer = layers.size() - 1; indexLayer >= 1; indexLayer--) {
            for (Neuron currentNeuron : layers.get(indexLayer)) {
                for (Neuron linkableNeuron : layers.get(indexLayer - 1)) {
                    currentNeuron.addArc(linkableNeuron, randomizeWeight ? rand.nextDouble() / 1000. : 1.0);
                }
            }
        }
    }

    public void serialize(String path) throws IOException {
        List<List<Double>> inputToHiddenWeights = new ArrayList<>();
        List<Double> hiddenToOutputWeights = new ArrayList<>();
        for (int inputNeuronIndex = 0; inputNeuronIndex < layers.get(0).size(); inputNeuronIndex++) {
            final Neuron inputNeuron = layers.get(0).get(inputNeuronIndex);
            inputToHiddenWeights.add(new ArrayList<>());
            for (int hiddenNeuronIndex = 0; hiddenNeuronIndex < layers.get(1).size(); hiddenNeuronIndex++) {
                final Neuron hiddenNeuron = layers.get(1).get(hiddenNeuronIndex);
                inputToHiddenWeights.get(inputNeuronIndex).add(hiddenNeuron.getArc(inputNeuron).getWeight());
            }
        }
        final Neuron outputNeuron = layers.get(2).get(0);
        for (int hiddenNeuronIndex = 0; hiddenNeuronIndex < layers.get(1).size(); hiddenNeuronIndex++) {
            final Neuron hiddenNeuron = layers.get(1).get(hiddenNeuronIndex);
            hiddenToOutputWeights.add(outputNeuron.getArc(hiddenNeuron).getWeight());
        }
        new ObjectMapper().writeValue(new File(path), new NetworkSerial(inputToHiddenWeights, hiddenToOutputWeights));
    }

    public static <T extends Network> T getNetwork(String path, Class clazz) throws IOException {
        return (T) new ObjectMapper().readValue(new File(path), clazz);
    }

    private static class NetworkSerial{

        @JsonProperty("inputToHiddenWeights")
        private final List<List<Double>> inputToHiddenWeights;

        @JsonProperty("hiddenToOutputWeights")
        private final List<Double> hiddenToOutputWeights;

        private NetworkSerial(List<List<Double>> inputToHiddenWeights, List<Double> hiddenToOutputWeights) {
            this.inputToHiddenWeights = inputToHiddenWeights;
            this.hiddenToOutputWeights = hiddenToOutputWeights;
        }
    }

    @JsonCreator
    public Network(@JsonProperty("inputToHiddenWeights") List<List<Double>> inputToHiddenWeights, @JsonProperty("hiddenToOutputWeights") List<Double> hiddenToOutputWeights){
        this(inputToHiddenWeights.size(), hiddenToOutputWeights.size());
        final Neuron outputNeuron = layers.get(2).get(0);
        for (int inputNeuronIndex = 0; inputNeuronIndex < layers.get(0).size(); inputNeuronIndex++) {
            final Neuron inputNeuron = layers.get(0).get(inputNeuronIndex);
            for (int hiddenNeuronIndex = 0; hiddenNeuronIndex < layers.get(1).size(); hiddenNeuronIndex++) {
                final Neuron hiddenNeuron = layers.get(1).get(hiddenNeuronIndex);
                hiddenNeuron.getArc(inputNeuron).setWeight(inputToHiddenWeights.get(inputNeuronIndex).get(hiddenNeuronIndex));
                outputNeuron.getArc(hiddenNeuron).setWeight(hiddenToOutputWeights.get(hiddenNeuronIndex));
            }
        }
    }

    public Network(){}

    public Network(int ... nbNeuronByLayer){
        this(true, nbNeuronByLayer);
    }

    public double getActivation(){
        // Last layer has only one neuron (the output neuron)
        return layers.get(layers.size() - 1).get(0).activation();
    }

    // Used for debug purposes
    @Override
    public String toString() {
        return "" + getActivation();
    }

    public void resetInputs() {
        for (Neuron neuron : layers.get(0)) {
            ((InputNeuron)neuron).setActivated(false);
        }
    }
}
