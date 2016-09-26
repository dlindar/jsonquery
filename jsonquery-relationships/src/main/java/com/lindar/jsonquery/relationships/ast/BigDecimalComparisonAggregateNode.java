package com.lindar.jsonquery.relationships.ast;

import java.math.BigDecimal;

/**
 * Created by stevenhills on 25/09/2016.
 */
public class BigDecimalComparisonAggregateNode extends NumberComparisonAggregateNode<BigDecimal> {
    public <R, C> R accept(JsonQueryAggregateVisitor<R, C> v, C context) {
        return v.visit(this, context);
    }

    public <R, C> R accept(JsonQueryAggregateVisitor<R, C> v) {
        return v.visit(this, null);
    }
}
