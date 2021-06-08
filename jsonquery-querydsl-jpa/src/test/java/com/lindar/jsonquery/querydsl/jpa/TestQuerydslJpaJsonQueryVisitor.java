package com.lindar.jsonquery.querydsl.jpa;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.collect.Lists;
import com.lindar.jsonquery.JsonQuery;
import com.lindar.jsonquery.ast.*;
import com.lindar.jsonquery.querydsl.jpa.domain.Player;
import com.lindar.jsonquery.querydsl.jpa.domain.PlayerAttrition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.JPQLSerializer;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by stevenhills on 25/09/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:**/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
public class TestQuerydslJpaJsonQueryVisitor {

    @PersistenceContext
    private EntityManager entityManager;

    private QuerydslJpaJsonQueryVisitor visitor;
    private PathBuilder<Player> playerEntity;
    private PathBuilder<PlayerAttrition> playerAttritionEntity;


    @Before
    public void setUp() {
        visitor = new QuerydslJpaJsonQueryVisitor(new JPAQuery());
        playerEntity = new PathBuilder(Player.class, "player");
        playerAttritionEntity = new PathBuilder(PlayerAttrition.class, "player_attrition");

    }

    /**
     * Tears down the test fixture.
     * (Called after every test case method.)
     */
    @After
    public void tearDown() {
        visitor = null;
        playerEntity = null;
        playerAttritionEntity = null;
    }



    @Test
    @DatabaseSetup("/sampleData.xml")
    public void testGeneratedQueryWithRelationships() throws Exception {

        EnumComparisonNode enumNode = new EnumComparisonNode();
        enumNode.setField("affiliate.type");
        enumNode.setOperation(EnumComparisonOperation.EQUALS);
        ArrayList<String> values = new ArrayList<String>();
        values.add("ORGANIC");
        enumNode.setValue(values);


        BigDecimalComparisonNode decimalNode = new BigDecimalComparisonNode();
        decimalNode.setField("deposits");
        decimalNode.setOperation(NumberComparisonOperation.GREATER_THAN);
        ArrayList<BigDecimal> decimalValues = new ArrayList<BigDecimal>();
        decimalValues.add(BigDecimal.ZERO);
        decimalNode.setValue(decimalValues);

        RelatedRelationshipNode relatedRelationshipNode = new RelatedRelationshipNode();
        relatedRelationshipNode.setField("attritions");
        relatedRelationshipNode.getConditions().getItems().add(decimalNode);

        JsonQuery holder = new JsonQuery();
        holder.getConditions().getItems().add(enumNode);
        holder.getConditions().getItems().add(relatedRelationshipNode);
        holder.getConditions().getItems().add(relatedRelationshipNode);
        holder.getConditions().getItems().add(relatedRelationshipNode);
        holder.getConditions().getItems().add(relatedRelationshipNode);

        //query.fetch();

        JPAQuery query2 = new JPAQuery(entityManager);
        PathBuilder entity2 = new PathBuilder(Player.class, "player");
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QuerydslJpaJsonQuery.applyPredicateAsSubquery(booleanBuilder, entity2, holder);

        query2.select(entity2).from(entity2).where(booleanBuilder);

        System.out.println(toString(query2));

        List fetch = query2.fetch();
        System.out.println(fetch.size());
    }

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void testRelativeDateRange() throws Exception {

        DateComparisonNode enumNode = new DateComparisonNode();
        enumNode.setField("lastLoginDate");
        enumNode.setOperation(DateComparisonNode.Operation.RELATIVE);
        enumNode.setRelativeOperation(DateComparisonNode.RelativeOperation.DAY);
        enumNode.setRelativeValue(1);
        DateComparisonNode.RelativeDays relativeDays = new DateComparisonNode.RelativeDays();
        relativeDays.setFriday(true);
        relativeDays.setSaturday(true);
        relativeDays.setSunday(true);
        enumNode.setRelativeDays(relativeDays);

        JsonQuery holder = new JsonQuery();
        holder.getConditions().getItems().add(enumNode);

        //query.fetch();

        JPAQuery query2 = new JPAQuery(entityManager);
        PathBuilder entity2 = new PathBuilder(Player.class, "player");
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QuerydslJpaJsonQuery.applyPredicateAsSubquery(booleanBuilder, entity2, holder);

        query2.select(entity2).from(entity2).where(booleanBuilder);

        System.out.println(toString(query2));

        List fetch = query2.fetch();
        System.out.println(fetch.size());
    }

    @Test
    public void testDayOfWeek(){
        LocalDate now = LocalDate.now().minusDays(2);
        System.out.println(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)));
        System.out.println(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY)));
        System.out.println(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY)));
        System.out.println(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY)));
        System.out.println(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)));
        System.out.println(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));
        System.out.println(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)));
        System.out.println("---");
        System.out.println(now.with(DayOfWeek.MONDAY));
        System.out.println(now.with(DayOfWeek.TUESDAY));
        System.out.println(now.with(DayOfWeek.WEDNESDAY));
        System.out.println(now.with(DayOfWeek.THURSDAY));
        System.out.println(now.with(DayOfWeek.FRIDAY));
        System.out.println(now.with(DayOfWeek.SATURDAY));
        System.out.println(now.with(DayOfWeek.SUNDAY));
    }

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void testDateTime() throws Exception {

        DateInstantComparisonNode enumNode = new DateInstantComparisonNode();
        enumNode.setField("lastLoginDate");
        enumNode.setOperation(BaseDateComparisonNode.Operation.RELATIVE);
        enumNode.setRelativeOperation(BaseDateComparisonNode.RelativeOperation.IN_THE_LAST);
        enumNode.setRelativeValue(1);
        enumNode.setRelativePeriod(BaseDateComparisonNode.RelativePeriod.HOUR);
        enumNode.setWithTime(true);


        JsonQuery holder = new JsonQuery();
        holder.getConditions().getItems().add(enumNode);

        //query.fetch();

        JPAQuery query2 = new JPAQuery(entityManager);
        PathBuilder entity2 = new PathBuilder(Player.class, "player");
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QuerydslJpaJsonQuery.applyPredicateAsSubquery(booleanBuilder, entity2, holder);

        query2.select(entity2).from(entity2).where(booleanBuilder);

        System.out.println(toString(query2));

        List fetch = query2.fetch();
        System.out.println(fetch.size());
    }


    @Test
    @DatabaseSetup("/sampleData.xml")
    public void testGeneratedQueryWithJoin() throws Exception {

        StringComparisonNode stringNode = new StringComparisonNode();
        stringNode.setField("brand.type");
        stringNode.setOperation(StringComparisonOperation.BEGINS_WITH);
        ArrayList<String> values = new ArrayList<String>();
        values.add("dragonfish");
        stringNode.setValue(values);


        BigDecimalComparisonNode decimalNode = new BigDecimalComparisonNode();
        decimalNode.setField("deposits");
        decimalNode.setOperation(NumberComparisonOperation.GREATER_THAN);
        ArrayList<BigDecimal> decimalValues = new ArrayList<BigDecimal>();
        decimalValues.add(BigDecimal.ZERO);
        decimalNode.setValue(decimalValues);

        RelatedRelationshipNode relatedRelationshipNode = new RelatedRelationshipNode();
        relatedRelationshipNode.setField("attritions");
        relatedRelationshipNode.getConditions().getItems().add(decimalNode);

        JsonQuery holder = new JsonQuery();
        holder.getConditions().getItems().add(stringNode);
        holder.getConditions().getItems().add(relatedRelationshipNode);

        //query.fetch();

        JPAQuery query2 = new JPAQuery(entityManager);
        PathBuilder entity2 = new PathBuilder(Player.class, "player");
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QuerydslJpaJsonQuery.applyPredicateAsSubquery(booleanBuilder, entity2, holder);

        query2.select(entity2).from(entity2).where(booleanBuilder);
        List fetch = query2.fetch();
        System.out.println(fetch.size());

        /*JPAQuery query2 = new JPAQuery(entityManager);
        query2.select(entity).from(entity).where(entity.in(query));
        query2.fetch();*/

        //assertToString("(select player from Player player where brand.type like ?1 escape '!' and brand.type like ?2 escape '!' and player in (select PlayerAttrition.player.id from PlayerAttrition PlayerAttrition where PlayerAttrition.deposits > ?3 group by PlayerAttrition.player.id))", query);

    }

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void testGeneratedQueryWithManyToMany() throws Exception {

        LookupComparisonNode lookupComparisonNode= new LookupComparisonNode();
        lookupComparisonNode.setField("lists");
        lookupComparisonNode.setOperation(LookupComparisonOperation.IN);
        ArrayList<Long> values = new ArrayList<Long>();
        values.add(1L);
        lookupComparisonNode.setValue(values);



        JsonQuery holder = new JsonQuery();
        holder.getConditions().getItems().add(lookupComparisonNode);

        //query.fetch();

        JPAQuery query2 = new JPAQuery(entityManager);
        PathBuilder entity2 = new PathBuilder(Player.class, "player");
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QuerydslJpaJsonQuery.applyPredicateAsSubquery(booleanBuilder, entity2, holder);

        query2.select(entity2).from(entity2).where(booleanBuilder);
        List fetch = query2.fetch();
        System.out.println(fetch.size());

        /*JPAQuery query2 = new JPAQuery(entityManager);
        query2.select(entity).from(entity).where(entity.in(query));
        query2.fetch();*/

        //assertToString("(select player from Player player where brand.type like ?1 escape '!' and brand.type like ?2 escape '!' and player in (select PlayerAttrition.player.id from PlayerAttrition PlayerAttrition where PlayerAttrition.deposits > ?3 group by PlayerAttrition.player.id))", query);

    }

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void testGeneratedQueryWithJoinAndManyToOne() throws Exception {
        LookupComparisonNode stringNode = new LookupComparisonNode();
        stringNode.setField("brand.id");
        stringNode.setOperation(LookupComparisonOperation.EQUALS);
        ArrayList<Long> values = new ArrayList<Long>();
        values.add(1L);
        stringNode.setValue(values);


        BigDecimalComparisonNode decimalNode = new BigDecimalComparisonNode();
        decimalNode.setField("deposits");
        decimalNode.setOperation(NumberComparisonOperation.EQUALS);
        ArrayList<BigDecimal> values2 = new ArrayList<BigDecimal>();
        values2.add(BigDecimal.ONE);
        decimalNode.setValue(values2);

        RelatedRelationshipNode relatedRelationshipNode = new RelatedRelationshipNode();
        relatedRelationshipNode.setField("attritions");
        relatedRelationshipNode.getConditions().getItems().add(decimalNode);
        relatedRelationshipNode.getConditions().getItems().add(stringNode);

        JsonQuery holder = new JsonQuery();
        holder.getConditions().getItems().add(relatedRelationshipNode);

        //query.fetch();

        JPAQuery query2 = new JPAQuery(entityManager);
        PathBuilder entity2 = new PathBuilder(Player.class, "player");
        query2.select(entity2).from(entity2);
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(QuerydslJpaJsonQuery.toPredicate(query2, entity2, holder));

        query2.where(booleanBuilder);

        //assertToString("s", query2);

        List fetch = query2.fetch();
        System.out.println(fetch.size());
    }

    @Test
    @DatabaseSetup("/sampleData.xml")
    public void testGeneratedQueryWithMultipleJoins() throws Exception {

        LookupComparisonNode lookupComparisonNode= new LookupComparisonNode();
        lookupComparisonNode.setField("brand.id");
        lookupComparisonNode.setOperation(LookupComparisonOperation.IN);
        ArrayList<Long> values = new ArrayList<Long>();
        values.add(1L);
        lookupComparisonNode.setValue(values);

        LookupComparisonNode lookupAffiliate = new LookupComparisonNode();
        lookupAffiliate.setField("affiliate.id");
        lookupAffiliate.setOperation(LookupComparisonOperation.IN);
        ArrayList<Long> valuesAffiliate = new ArrayList<Long>();
        valuesAffiliate.add(1L);
        lookupAffiliate.setValue(valuesAffiliate);


        JsonQuery holder = new JsonQuery();
        holder.getConditions().getItems().add(lookupComparisonNode);
        holder.getConditions().getItems().add(lookupAffiliate);


        //query.fetch();

        JPAQuery query2 = new JPAQuery(entityManager);
        PathBuilder entity2 = new PathBuilder(Player.class, "player");
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QuerydslJpaJsonQuery.applyPredicateAsSubquery(booleanBuilder, entity2, holder);

        query2.select(entity2).from(entity2).where(booleanBuilder);
        List fetch = query2.fetch();
        System.out.println(fetch.size());

        assertToString("(select player from Player player where player in (select player from Player player   left join player.brand as brand   left join player.affiliate as affiliate where brand.id = ?1 and affiliate.id = ?1))", query2);

        /*JPAQuery query2 = new JPAQuery(entityManager);
        query2.select(entity).from(entity).where(entity.in(query));
        query2.fetch();*/

        //assertToString("(select player from Player player where brand.type like ?1 escape '!' and brand.type like ?2 escape '!' and player in (select PlayerAttrition.player.id from PlayerAttrition PlayerAttrition where PlayerAttrition.deposits > ?3 group by PlayerAttrition.player.id))", query);

    }





    @Test
    public void testVisitStringComparisonNode() {
        List<String> value = Lists.newArrayList("test");
        List<String> values = Lists.newArrayList("test", "another test");


        assertToString("player.promocode = ?1",
                createStringComparisonNodePredicate("promocode",
                        StringComparisonOperation.EQUALS,
                        value));

        assertToString("player.promocode like ?1 escape '!'",
                createStringComparisonNodePredicate("promocode",
                        StringComparisonOperation.BEGINS_WITH,
                        value));

        assertToString("player.promocode like ?1 escape '!'",
                createStringComparisonNodePredicate("promocode",
                        StringComparisonOperation.CONTAINS,
                        value));

        assertToString("player.promocode = ?1",
                createStringComparisonNodePredicate("promocode",
                        StringComparisonOperation.EMPTY,
                        value));

        assertToString("player.promocode like ?1 escape '!'",
                createStringComparisonNodePredicate("promocode",
                        StringComparisonOperation.ENDS_WITH,
                        value));

        assertToString("player.promocode like ?1 escape '!'",
                createStringComparisonNodePredicate("promocode",
                        StringComparisonOperation.REGEX,
                        value));

        assertToString("player.promocode in (?1)",
                createStringComparisonNodePredicate("promocode",
                        StringComparisonOperation.IN,
                        values));

        assertToString("not player.promocode = ?1",
                createStringComparisonNodePredicate("promocode",
                        StringComparisonOperation.EQUALS,
                        values, true));

        assertToString("brand.type = ?1",
                createStringComparisonNodePredicate("brand.type",
                        StringComparisonOperation.EQUALS,
                        value));
    }

    @Test
    public void testVisitBigDecimalComparisonNode() {
        List<BigDecimal> value = Lists.newArrayList(BigDecimal.ZERO);
        List<BigDecimal> values = Lists.newArrayList(BigDecimal.ZERO, BigDecimal.TEN);

        assertToString("player.deposits = ?1",
                createBigDecimalComparisonNodePredicate("deposits",
                        NumberComparisonOperation.EQUALS,
                        value));

        assertToString("player.deposits > ?1",
                createBigDecimalComparisonNodePredicate("deposits",
                        NumberComparisonOperation.GREATER_THAN,
                        value));

        assertToString("player.deposits >= ?1",
                createBigDecimalComparisonNodePredicate("deposits",
                        NumberComparisonOperation.GREATER_THAN_OR_EQUAL,
                        value));

        assertToString("player.deposits < ?1",
                createBigDecimalComparisonNodePredicate("deposits",
                        NumberComparisonOperation.LESS_THAN,
                        value));

        assertToString("player.deposits <= ?1",
                createBigDecimalComparisonNodePredicate("deposits",
                        NumberComparisonOperation.LESS_THAN_OR_EQUAL,
                        value));

        assertToString("player.deposits between ?1 and ?2",
                createBigDecimalComparisonNodePredicate("deposits",
                        NumberComparisonOperation.BETWEEN,
                        values));

        assertToString("player.deposits in (?1)",
                createBigDecimalComparisonNodePredicate("deposits",
                        NumberComparisonOperation.IN,
                        values));

        assertToString("player.deposits is null",
                createBigDecimalComparisonNodePredicate("deposits",
                        NumberComparisonOperation.EMPTY,
                        value));

        assertToString("not player.deposits = ?1",
                createBigDecimalComparisonNodePredicate("deposits",
                        NumberComparisonOperation.EQUALS,
                        value, true));
    }



    @Test
    public void testVisitLogicalNode(){
        List<BigDecimal> value = Lists.newArrayList(BigDecimal.ZERO);

        BigDecimalComparisonNode node1 = new BigDecimalComparisonNode();
        node1.setField("deposits");
        node1.setNegate(false);
        node1.setOperation(NumberComparisonOperation.LESS_THAN);
        node1.setValue(value);

        BigDecimalComparisonNode node2 = new BigDecimalComparisonNode();
        node2.setField("deposits");
        node2.setNegate(false);
        node2.setOperation(NumberComparisonOperation.GREATER_THAN);
        node2.setValue(value);

        BigDecimalComparisonNode node3 = new BigDecimalComparisonNode();
        node3.setField("deposits");
        node3.setNegate(false);
        node3.setOperation(NumberComparisonOperation.LESS_THAN_OR_EQUAL);
        node3.setValue(value);

        BigDecimalComparisonNode node4 = new BigDecimalComparisonNode();
        node4.setField("deposits");
        node4.setNegate(false);
        node4.setOperation(NumberComparisonOperation.GREATER_THAN_OR_EQUAL);
        node4.setValue(value);

        StringComparisonNode nodeRelated = new StringComparisonNode();
        nodeRelated.setField("brand.type");
        nodeRelated.setNegate(false);
        nodeRelated.setOperation(StringComparisonOperation.CONTAINS);
        nodeRelated.setValue(Lists.newArrayList("something"));


        LogicalNode logicalNodeAnd = new LogicalNode(LogicalNode.LogicalOperation.AND);
        logicalNodeAnd.setItems(Lists.newArrayList(node3, node4));

        LogicalNode logicalNodeOr = new LogicalNode(LogicalNode.LogicalOperation.OR);
        logicalNodeOr.setItems(Lists.newArrayList(node3, node4));

        assertToString("player.deposits < ?1 and player.deposits > ?1",
                createLogicalNodePredicate(LogicalNode.LogicalOperation.AND, Lists.newArrayList(node1, node2))
        );

        assertToString("player.deposits < ?1 or player.deposits > ?1",
                createLogicalNodePredicate(LogicalNode.LogicalOperation.OR, Lists.newArrayList(node1, node2))
        );

        assertToString("player.deposits < ?1 and (player.deposits <= ?1 and player.deposits >= ?1)",
                createLogicalNodePredicate(LogicalNode.LogicalOperation.AND, Lists.newArrayList(node1, logicalNodeAnd))
        );

        assertToString("player.deposits < ?1 and (player.deposits <= ?1 or player.deposits >= ?1)",
                createLogicalNodePredicate(LogicalNode.LogicalOperation.AND, Lists.newArrayList(node1, logicalNodeOr))
        );

        assertToString("player.deposits < ?1 or player.deposits <= ?1 and player.deposits >= ?1",
                createLogicalNodePredicate(LogicalNode.LogicalOperation.OR, Lists.newArrayList(node1, logicalNodeAnd))
        );

        assertToString("player.deposits < ?1 or (player.deposits <= ?1 or player.deposits >= ?1)",
                createLogicalNodePredicate(LogicalNode.LogicalOperation.OR, Lists.newArrayList(node1, logicalNodeOr))
        );


        assertToString("brand.type like ?1 escape '!'",
                createLogicalNodePredicate(LogicalNode.LogicalOperation.AND, Lists.newArrayList(nodeRelated))
        );

    }


    public static class Holder {
        public LogicalNode conditions = new LogicalNode(LogicalNode.LogicalOperation.AND);
        public LogicalRelationshipNode relationships = new LogicalRelationshipNode(LogicalRelationshipNode.LogicalOperation.AND);
    }






    private Predicate createStringComparisonNodePredicate(String field,
                                                          StringComparisonOperation comparisonOperation,
                                                          List<String> value
    ){
        return createStringComparisonNodePredicate(field, comparisonOperation, value, false);

    }

    private Predicate createStringComparisonNodePredicate(String field,
                                                          StringComparisonOperation comparisonOperation,
                                                                  List<String> value,
                                                          boolean negate
    ){
        StringComparisonNode node = new StringComparisonNode();
        node.setField(field);
        node.setNegate(negate);
        node.setOperation(comparisonOperation);
        node.setValue(value);

        return node.accept(visitor, playerEntity);

    }

    private Predicate createBigDecimalComparisonNodePredicate(String field,
                                                              NumberComparisonOperation comparisonOperation,
                                                              List<BigDecimal> value
    ){
        return createBigDecimalComparisonNodePredicate(field, comparisonOperation, value, false);
    }

    private Predicate createBigDecimalComparisonNodePredicate(String field,
                                                              NumberComparisonOperation comparisonOperation,
                                                              List<BigDecimal> value,
                                                              boolean negate
    ){
        BigDecimalComparisonNode node = new BigDecimalComparisonNode();
        node.setField(field);
        node.setNegate(negate);
        node.setOperation(comparisonOperation);
        node.setValue(value);

        return node.accept(visitor, playerEntity);
    }

    private Predicate createLogicalNodePredicate(LogicalNode.LogicalOperation operation, List<Node> nodes){
        LogicalNode node = new LogicalNode(operation);
        node.setItems(nodes);
        return node.accept(visitor, playerEntity);
    }

    protected static void assertToString(String expected, Expression<?> expr) {
        JPQLSerializer serializer = new JPQLSerializer(HQLTemplates.DEFAULT, null);
        assertEquals(expected, serializer.handle(expr).toString().replace("\n", " "));
    }

    protected static String toString(Expression<?> expr) {
        JPQLSerializer serializer = new JPQLSerializer(HQLTemplates.DEFAULT, null);
        return serializer.handle(expr).toString().replace("\n", " ");
    }
}
