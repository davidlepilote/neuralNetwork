package neuralnetwork;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by David et Monireh on 16/10/2016.
 */
public class Neuron implements Iterable<Neuron.Arc> {

    public class Arc{

        // The input neuron
        public final Neuron neuron;

        private double weight;

        public double eligibilityTrace = 0.;

        public Arc(Neuron neuron, double weight) {
            this.neuron = neuron;
            this.weight = weight;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            Neuron.this.shouldUpdate = true;
            this.weight = weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Arc arc = (Arc) o;

            return neuron != null ? neuron.equals(arc.neuron) : arc.neuron == null;

        }

        @Override
        public int hashCode() {
            return neuron != null ? neuron.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "" + weight;
        }
    }

    private Map<Neuron, Arc> inputs = new HashMap<>();

    protected boolean shouldUpdate = true;

    private double activation = 0.;

    public void addArc(Neuron linkableNeuron, double weight) {
        if(!linkableNeuron.hasNeuron(this)){
            final Arc arc = new Arc(linkableNeuron, weight);
            inputs.put(linkableNeuron, arc);
        }
    }

    public Arc getArc(Neuron hiddenNeuron) {
        return inputs.get(hiddenNeuron);
    }

    private boolean shouldUpdate(){
        if(shouldUpdate){
            return true;
        } else {
            for (Arc input : inputs.values()) {
                if(input.neuron.shouldUpdate()){
                    shouldUpdate = true;
                    return true;
                }
            }
        }
        return false;
    }

    public double activation(){
        if(shouldUpdate()){
            double weightedSum = 0.;
            for (Arc input : inputs.values()) {
                weightedSum += input.weight * input.neuron.activation();
            }
            activation = 1. / (1. + Math.exp(-weightedSum));
            this.shouldUpdate = false;
        }
        return activation;
    }

    private boolean hasNeuron(Neuron neuron) {
        return inputs.containsKey(neuron);
    }

    // Used for debug purposes
    @Override
    public String toString() {
        return "" + activation();
    }

    @Override
    public Iterator<Arc> iterator() {
        return inputs.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super Arc> action) {
        inputs.values().forEach(action);
    }

    @Override
    public Spliterator<Arc> spliterator() {
        return inputs.values().spliterator();
    }
}
