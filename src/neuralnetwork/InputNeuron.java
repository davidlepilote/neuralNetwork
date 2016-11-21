package neuralnetwork;

/**
 * Created by David et Monireh on 16/10/2016.
 */
public class InputNeuron extends Neuron{

    private boolean activated = false;

    @Override
    public double activation() {
        return isActivated() ? 1 : 0;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
        shouldUpdate = true;
    }

}
