package gr.demokritos.iit.jinsect.encoders;

import java.util.Map;
import java.util.HashMap;

/**
 * This class assign weights (codes) to a set of vertex labels
 * in order to be used for vertex coding in similarity measures.
 *
 * The first label to be added is initialized with a starting value,
 * and subsequent additions increase the value associated with the
 * added vertices.
 *
 * @author VHarisop
 */
public class VertexCoder extends HashMap<String, Double> {
	static final long serialVersionUID = 1L; 
	static final double INITIAL_VALUE = 1.0;

	/**
	 * The weight value to be associated with added keys.
	 */
	protected double weightVal = INITIAL_VALUE;

	/**
	 * The step by which {@link #weightVal} should be increased
	 * when a new key is added.
	 */
	protected double step = 0.00005;

	/**
	 * Creates a new VertexCoder object with default parameters.
	 */
	public VertexCoder() {
		super();
	}

	/**
	 * Creates a new VertexCoder object with a given initial capacity.
	 * 
	 * @param initialCapacity the initial capacity
	 */
	public VertexCoder(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Creates a new VertexCoder object with a given initial capacity
	 * and a load factor.
	 *
	 * @param initialCapacity the initial capacity
	 * @param loadFactor the load factor
	 */
	public VertexCoder(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
	 * Creates a new VertexCoder object with the same mappings as the
	 * specified map. The object is created with default load factor (0.75) 
	 * and an initial capacity sufficient to hold the specified map.
	 *
	 * @param m the map whose mappings are to be placed in this map.
	 */
	public VertexCoder(Map<? extends String, ? extends Double> m) {
		super(m);
	}

	/**
	 * Sets this VertexCoder's step to a new value and returns the VertexCoder
	 * itself. Useful for chaining after calling the constructor.
	 *
	 * @param newStep the new step value
	 * @return the modified VertexCoder object
	 */
	public VertexCoder withStep(double newStep) {
		this.step = newStep;
		return this;
	}

	/**
	 * Sets this VertexCoder's initial weight value to a new one and returns the
	 * modified VertexCoder object itself.
	 *
	 * @param newVal the new initial weight value
	 * @return the modified VertexCoder object
	 */
	public VertexCoder withWeightValue(double newVal) {
		this.weightVal = newVal;
		return this;
	}

	/**
	 * Adds a new weight mapping for a given label, returning the value
	 * it is associated to. 
	 * If a mapping already existed, returns the old value 
	 * associated with the key.
	 *
	 * @param key the label to add
	 * @return the value associated with the key
	 */
	public Double putLabel(String key) {
		Double oldVal = super.get(key);
		if (oldVal != null)
			return oldVal;
		else {
			super.put(key, weightVal);
			weightVal += step;
			return (weightVal - step);
		}
	}

	/**
	 * Gets the weight associated with a provided key. If the key
	 * is not found in the backing map, it is added automatically
	 * and its assigned value is returned.
	 *
	 * @param key the key whose weight is requested
	 * @return the key's associated weight
	 */
	public Double getLabel(String key) {
		if (!super.containsKey(key)) {
			Double newVal = this.putLabel(key);
			return newVal;
		}
		else {
			return super.get(key);
		}
	}

	/**
	 * Removes all the mappings for the map. Also resets 
	 * {@link #weightVal} to its initial value.
	 */
	public void clear() {
		super.clear();
		weightVal = INITIAL_VALUE;
	}
}


