package com.nosqlrevolution;

/**
 *
 * @author cbrown
 */
public class QueryExpression {
    private Object field;
    private Operator operator;
    private Object value;
    
    public static QueryExpression and(Object field) {
        return new QueryExpression().setField(field);
    }
    
    public static QueryExpression or(Object field) {
        return new QueryExpression().setField(field);
    }
    
    public static QueryExpression not(Object field) {
        return new QueryExpression().setField(field);
    }
    
    public static QueryExpression field(Object field) {
        return new QueryExpression().setField(field);
    }
    
    
    public QueryExpression equal(Object value) {
        return this.setOperator(Operator.EQUAL).setValue(value);
    }
    
    public QueryExpression notEqual(Object value) {
        return this.setOperator(Operator.NOT_EQUAL).setValue(value);
    }
    
    public QueryExpression in(Object value) {
        return this.setOperator(Operator.IN).setValue(value);
    }
    
    public QueryExpression notIn(Object value) {
        return this.setOperator(Operator.NOT_IN).setValue(value);
    }
    
    public QueryExpression mustContain(Object value) {
        return this.setOperator(Operator.MUST).setValue(value);
    }
    
    public QueryExpression shouldContain(Object value) {
        return this.setOperator(Operator.SHOULD).setValue(value);
    }
    
    public QueryExpression mustNotContain(Object value) {
        return this.setOperator(Operator.MUST_NOT).setValue(value);
    }
    
    public Object getField() {
        return field;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getValues() {
        return value;
    }

    private QueryExpression setField(Object field) {
        this.field = field;
        return this;
    }
    
    private QueryExpression setOperator(Operator operator) {
        this.operator = operator;
        return this;
    }
    
    private QueryExpression setValue(Object value) {
        this.value = value;
        return this;
    }
}

