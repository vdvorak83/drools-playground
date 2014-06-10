package com.plugtree.training;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.drools.base.BaseEvaluator;
import org.drools.base.ValueType;
import org.drools.base.evaluators.EvaluatorCache;
import org.drools.base.evaluators.EvaluatorDefinition;
import org.drools.base.evaluators.Operator;
import org.drools.common.InternalFactHandle;
import org.drools.common.InternalWorkingMemory;
import org.drools.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.rule.VariableRestriction.VariableContextEntry;
import org.drools.spi.Evaluator;
import org.drools.spi.FieldValue;
import org.drools.spi.InternalReadAccessor;

public class DisjointEvaluatorsDefinition implements EvaluatorDefinition {
	public static final Operator DISJOINT = Operator.addOperatorToRegistry(
			"disjoint", false);
	public static final Operator NOT_DISJOINT = Operator.addOperatorToRegistry(
			"disjoint", true);

	private static final String[] SUPPORTED_IDS = { DISJOINT
			.getOperatorString() };

	private EvaluatorCache evaluators = new EvaluatorCache() {
		private static final long serialVersionUID = 510l;
		{
			addEvaluator(ValueType.OBJECT_TYPE, DISJOINT,
					DisjointEvaluator.INSTANCE);
			addEvaluator(ValueType.OBJECT_TYPE, NOT_DISJOINT,
					NotDisjointEvaluator.INSTANCE);
		}
	};

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		evaluators = (EvaluatorCache) in.readObject();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(evaluators);
	}

	public Evaluator getEvaluator(ValueType type, Operator operator) {
		return this.evaluators.getEvaluator(type, operator);
	}

	public Evaluator getEvaluator(ValueType type, Operator operator,
			String parameterText) {
		return this.evaluators.getEvaluator(type, operator);
	}

	public Evaluator getEvaluator(final ValueType type,
			final String operatorId, final boolean isNegated,
			final String parameterText) {
		return this.getEvaluator(type, operatorId, isNegated, parameterText,
				Target.FACT, Target.FACT);
	}

	public Evaluator getEvaluator(final ValueType type,
			final String operatorId, final boolean isNegated,
			final String parameterText, final Target left, final Target right) {
		return this.evaluators.getEvaluator(type,
				Operator.determineOperator(operatorId, isNegated));
	}

	public String[] getEvaluatorIds() {
		return SUPPORTED_IDS;
	}

	public boolean isNegatable() {
		return true;
	}

	public Target getTarget() {
		return Target.FACT;
	}

	public boolean supportsType(ValueType type) {
		return this.evaluators.supportsType(type);
	}

	public static class DisjointEvaluator extends BaseEvaluator {

		private static boolean isIntersectionEmpty(Collection<?> a, Collection<?> b) {
			if (a == null || b == null)
				return true;
			Set<?> h = new HashSet<Object>(a);
			h.retainAll(b);
			return h.isEmpty();
		}

		private static final long serialVersionUID = 510l;
		public final static Evaluator INSTANCE = new DisjointEvaluator();

		public DisjointEvaluator() {
			super(ValueType.OBJECT_TYPE, DISJOINT);
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory,
				InternalReadAccessor extractor, InternalFactHandle factHandle,
				FieldValue value) {
			final Collection<?> set1 = (Collection<?>) extractor.getValue( workingMemory, factHandle.getObject() );
            final Collection<?> set2 = (Collection<?>) value.getValue();
			return isIntersectionEmpty(set1, set2);
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory,
				InternalReadAccessor leftExtractor, InternalFactHandle left,
				InternalReadAccessor rightExtractor, InternalFactHandle right) {
			final Collection<?> set1 = (Collection<?>) leftExtractor.getValue( workingMemory, left.getObject() );
			final Collection<?> set2 = (Collection<?>) rightExtractor.getValue( workingMemory, right.getObject() );
			return isIntersectionEmpty(set1, set2);
		}

		@Override
		public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
				VariableContextEntry context, InternalFactHandle right) {
			final Collection<?> set1 = (Collection<?>) context.extractor.getValue( workingMemory, right.getObject() );
			final Collection<?> set2 = (Collection<?>) ((ObjectVariableContextEntry) context).right;
            return isIntersectionEmpty(set1, set2);
		}

		@Override
		public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
				VariableContextEntry context, InternalFactHandle left) {
			final Collection<?> set1 = (Collection<?>) ((ObjectVariableContextEntry) context).right;
			final Collection<?> set2 = (Collection<?>) context.declaration.getExtractor().getValue( workingMemory, left.getObject() );
            return isIntersectionEmpty(set1, set2);
		}

		public String toString() {
			return "Set disjoint";
		}
	}

	public static class NotDisjointEvaluator extends BaseEvaluator {

		private static boolean isIntersectionNotEmpty(Collection<?> a, Collection<?> b) {
			if (a == null || b == null)
				return false;
			Set<?> h = new HashSet<Object>(a);
			h.retainAll(b);
			return !h.isEmpty();
		}

		private static final long serialVersionUID = 510l;
		public final static Evaluator INSTANCE = new NotDisjointEvaluator();

		public NotDisjointEvaluator() {
			super(ValueType.OBJECT_TYPE, NOT_DISJOINT);
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory,
				InternalReadAccessor extractor, InternalFactHandle factHandle,
				FieldValue value) {
			final Collection<?> set1 = (Collection<?>) extractor.getValue( workingMemory, factHandle.getObject() );
            final Collection<?> set2 = (Collection<?>) value.getValue();
			return isIntersectionNotEmpty(set1, set2);
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory,
				InternalReadAccessor leftExtractor, InternalFactHandle left,
				InternalReadAccessor rightExtractor, InternalFactHandle right) {
			final Collection<?> set1 = (Collection<?>) leftExtractor.getValue( workingMemory, left.getObject() );
			final Collection<?> set2 = (Collection<?>) rightExtractor.getValue( workingMemory, right.getObject() );
			return isIntersectionNotEmpty(set1, set2);
		}

		@Override
		public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
				VariableContextEntry context, InternalFactHandle right) {
			final Collection<?> set1 = (Collection<?>) context.extractor.getValue( workingMemory, right.getObject() );
			final Collection<?> set2 = (Collection<?>) ((ObjectVariableContextEntry) context).right;
            return isIntersectionNotEmpty(set1, set2);
		}

		@Override
		public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
				VariableContextEntry context, InternalFactHandle left) {
			final Collection<?> set1 = (Collection<?>) ((ObjectVariableContextEntry) context).right;
			final Collection<?> set2 = (Collection<?>) context.declaration.getExtractor().getValue( workingMemory, left.getObject() );
            return isIntersectionNotEmpty(set1, set2);
		}
	}
}