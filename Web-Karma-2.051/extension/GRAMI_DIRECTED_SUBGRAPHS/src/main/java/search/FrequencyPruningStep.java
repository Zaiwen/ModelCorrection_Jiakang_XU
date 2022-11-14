/**
 * created May 25, 2006
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package search;

import dataStructures.*;

import java.util.Collection;
import java.util.TreeSet;


/**
 * This class implements the general pruning of fragments according to their
 * frequency.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class FrequencyPruningStep<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {

	private final Frequency min, max;
	private TreeSet<Frequency> freqs = new TreeSet<>();


	/**
	 * creates a new frequency pruning
	 * 
	 * @param next
	 * @param min
	 * @param max
	 */
	public FrequencyPruningStep(final MiningStep<NodeType, EdgeType> next,
			final Frequency min, final Frequency max) {
		super(next);
		this.min = min;
		this.max = max;
		reset();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      java.util.Collection)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {


		final Frequency freq = ((Frequented) node).frequency();  //HERE THE FREQUENCY CALCULATION OCCURS !!!

//		System.err.println(freqs);
//		if (freqs.first().compareTo(freq) < 0 && min.compareTo(freq) <= 0){
//			freqs.add(freq);
//			if (freqs.size() > 3){
//				freqs.remove(freqs.first());
//			}
//			callNext(node, extensions);
//		}else{
//			node.store(false);
//		}
//
//

		if (max != null && max.compareTo(freq) < 0) {
			node.store(false);
			node.stopExtend(true);
		}

		if (min.compareTo(freq) > 0) {
//			System.err.println(min);
//			System.err.println(freq);
			node.store(false);
			node.stopExtend(true);
//			System.err.println("min > freq");
		}
		else {
//			System.err.println("freq next:"+next.getClass());
			callNext(node, extensions);
		}
//		callNext(node, extensions);

	}

	@Override
	protected void reset() {
		freqs.clear();
		freqs.add(new IntFrequency(0));
	}

}
