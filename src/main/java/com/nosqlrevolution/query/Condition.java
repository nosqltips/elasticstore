package com.nosqlrevolution.query;

/**
 *
 * @author cbrown
 */
public class Condition {
    private String field;
    private Field specialField;
    private Operator operator;
    private String[] values;
    
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
                .setOperator(Operator.AND)
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
                .setOperator(Operator.NOT)
                .getCompletedCondition()
            );
    }
    
    /**
     * condition on field
     * @param field
     * @return 
     */
    public static Condition field(String field) {
        return new Condition().setField(field);
    }
    
    /**
     * condition on field
     * @param specialField
     * @return 
     */
    public static Condition field(Field specialField) {
        return new Condition().setField(specialField);
    }
    
    
    /**
     * equal condition
     * @param value
     * @return 
     */
    public CompletedCondition equal(String value) {
        return this.setOperator(Operator.EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * not equal condition
     * @param value 
     * @return  
     */
    public CompletedCondition notEqual(String value) {
        return this.setOperator(Operator.NOT_EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * less than condition
     * @param value 
     * @return  
     */
    public CompletedCondition lt(String value) {
        return this.setOperator(Operator.EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * less than or equal condition
     * @param value 
     * @return  
     */
    public CompletedCondition lte(String value) {
        return this.setOperator(Operator.NOT_EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * greater than condition
     * @param value
     * @return 
     */
    public CompletedCondition gt(String value) {
        return this.setOperator(Operator.EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * greater than or equal condition
     * @param value 
     * @return  
     */
    public CompletedCondition gte(String value) {
        return this.setOperator(Operator.NOT_EQUAL).setValue(value).getCompletedCondition();
    }
    
    /**
     * in condition 
     * @param values
     * @return 
     */
    public CompletedCondition in(String... values) {
        return this.setOperator(Operator.IN).setValues(values).getCompletedCondition();
    }
    
    /**
     * not in condition
     * @param values 
     * @return  
     */
    public CompletedCondition notIn(String... values) {
        return this.setOperator(Operator.NOT_IN).setValues(values).getCompletedCondition();
    }
    
    /**
     * must contain condition
     * @param value 
     * @return  
     */
    public CompletedCondition mustContain(String... value) {
        return this.setOperator(Operator.MUST).setValues(value).getCompletedCondition();
    }
    
    /**
     * should contain condition
     * @param values 
     * @return  
     */
    public CompletedCondition shouldContain(String... values) {
        return this.setOperator(Operator.SHOULD).setValues(values).getCompletedCondition();
    }
    
    /**
     * must not contain condition
     * @param values 
     * @return  
     */
    public CompletedCondition mustNotContain(String... values) {
        return this.setOperator(Operator.MUST_NOT).setValues(values).getCompletedCondition();
    }

    // Field operators
    private Condition setField(String field) {
        this.field = field;
        return this;
    }
    
    private Condition setField(Field specialField) {
        this.specialField = specialField;
        return this;
    }
    
    private Condition setOperator(Operator operator) {
        this.operator = operator;
        return this;
    }
    
    private Condition setValue(String value) {
        this.values = new String[]{value};
        return this;
    }
    
    private Condition setValues(String[] values) {
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
     * Inner class that is used to give public access to just the fields we want, and separate them from the parent.
     * This also helps us to better construct Condition objects as the CompletedCondition is returned 
     * when we've reached an end point in object construction;
     */
    public class CompletedCondition {

        /**
        * return the field
        * @return 
        */
        public String getField() {
            return field;
        }

        /**
        * return the field
        * @return 
        */
        public Field getSpecialField() {
            return specialField;
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
        public String getValue() {
            return values[0];
        }

        /**
        * return the values
        * @return 
        */
        public String[] getValues() {
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

