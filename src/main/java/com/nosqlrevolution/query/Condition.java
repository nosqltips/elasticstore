package com.nosqlrevolution.query;

/**
 *
 * @author cbrown
 */
public class Condition {
    private Object field;
    private Operator operator;
    private Object value;
    private Object[] values;
    
    private CompletedCondition[] conditions;    
    
    /**
     * and group condition
     * @param conditions
     * @return 
     */
    public static CompletedCondition and(CompletedCondition... conditions) {
        return (
                new Condition().setConditions(conditions)
                .setOperator(Operator.OR)
                .getCompletedCondition()
            );
    }
    
    /**
     * or group condition
     * @param conditions
     * @return 
     */
    public static CompletedCondition or(CompletedCondition... conditions) {
        return (
                new Condition().setConditions(conditions)
                .setOperator(Operator.OR)
                .getCompletedCondition()
            );
    }
    
    /**
     * not group condition
     * @param conditions
     * @return 
     */
    public static CompletedCondition not(CompletedCondition... conditions) {
        return (
                new Condition().setConditions(conditions)
                .setOperator(Operator.OR)
                .getCompletedCondition()
            );
    }
    
    /**
     * condition on field
     * @param field
     * @return 
     */
    public static Condition field(Object field) {
        return new Condition().setField(field);
    }
    
    /**
     * condition on entire document
     * @return 
     */
    public static Condition all() {
        return new Condition().setField("_all");
    }
    
    
    /**
     * equal condition
     * @param value
     * @return 
     */
    public CompletedCondition equal(Object value) {
        return this.setOperator(Operator.EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * not equal condition
     * @param value 
     */
    public CompletedCondition notEqual(Object value) {
        return this.setOperator(Operator.NOT_EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * less than condition
     * @param value 
     */
    public CompletedCondition lt(Object value) {
        return this.setOperator(Operator.EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * less than or equal condition
     * @param value 
     */
    public CompletedCondition lte(Object value) {
        return this.setOperator(Operator.NOT_EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * greater than condition
     * @param value
     * @return 
     */
    public CompletedCondition gt(Object value) {
        return this.setOperator(Operator.EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * greater than or equal condition
     * @param value 
     */
    public CompletedCondition gte(Object value) {
        return this.setOperator(Operator.NOT_EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * in condition
     * @param value 
     */
    public CompletedCondition in(Object... values) {
        return this.setOperator(Operator.IN).setValues(values).getCompletedCondition();
    }
    
    /**
     * not in condition
     * @param values 
     */
    public CompletedCondition notIn(Object... values) {
        return this.setOperator(Operator.NOT_IN).setValues(values).getCompletedCondition();
    }
    
    /**
     * must contain condition
     * @param value 
     */
    public CompletedCondition mustContain(Object... value) {
        return this.setOperator(Operator.MUST).setValues(value).getCompletedCondition();
    }
    
    /**
     * should contain condition
     * @param values 
     */
    public CompletedCondition shouldContain(Object... values) {
        return this.setOperator(Operator.SHOULD).setValues(values).getCompletedCondition();
    }
    
    /**
     * must not contain condition
     * @param values 
     */
    public CompletedCondition mustNotContain(Object... values) {
        return this.setOperator(Operator.MUST_NOT).setValues(values).getCompletedCondition();
    }

    // Field operators
    private Condition setField(Object field) {
        this.field = field;
        return this;
    }
    
    private Condition setOperator(Operator operator) {
        this.operator = operator;
        return this;
    }
    
    private Condition setValue(Object value) {
        this.value = value;
        return this;
    }
    
    private Condition setValues(Object[] values) {
        this.values = values;
        return this;
    }
    
    private Condition setConditions(CompletedCondition[] conditions) {
        this.conditions = conditions;
        return this;
    }
    
    /**
     * Used to return an instance of CompletedCondition for static and non-static references
     * @return 
     */
    private CompletedCondition getCompletedCondition() {
        return new CompletedCondition();
    }
    
    /**
     * Inner class that is used to give public access to just the fields we want, and separate them form the parent.
     * This also helps us to better construct Condition objects as the CompletedCondition is returned 
     * when we've reached an end point in object construction;
     */
    public class CompletedCondition {

        /**
        * return the field
        * @return 
        */
        public Object getField() {
            return field;
        }

        /**
        * return the operator
        * @return 
        */
        public Operator getOperator() {
            return operator;
        }

        /**
        * return the value
        * @return 
        */
        public Object getValue() {
            return value;
        }

        /**
        * return the values
        * @return 
        */
        public Object[] getValues() {
            return values;
        }

        /**
        * return conditions
        * @return 
        */
        public CompletedCondition[] getConditions() {
            return conditions;
        }
    }
}

