package org.concordion.markdown;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.StringBuilderVar;
import org.pegdown.Parser;

public class ConcordionPluginParser extends Parser {

    private static final java.lang.String OPENING = "`";
    private static final java.lang.String CLOSING = "}`";

    public ConcordionPluginParser() {
        super(ALL, 1000l, DefaultParseRunnerProvider);
    }
    
    public Rule concordionStatement() {
//        return commandWithText();
        return FirstOf(
            commandWithText(),
            commandNoText()
        );
    }
    
    public Rule commandWithText() {
        StringBuilderVar text = new StringBuilderVar();
        return NodeSequence(
            OPENING,
            OneOrMore(TestNot(" {"), ANY, text.append(matchedChar())),
            " {",
            FirstOf(setCommand(text), assertEqualsCommand(text), execute1Command(text)),
            CLOSING
        );
    }
    
    public Rule commandNoText() {
        StringBuilderVar text = new StringBuilderVar();
        return NodeSequence(
            "`",
            execute2Command(text),
            "`"
        );
    }
    
    public Rule setCommand(StringBuilderVar text) {
        StringBuilderVar varName = new StringBuilderVar();
        return NodeSequence(
            "#",
            OneOrMore(TestNot(CLOSING), BaseParser.ANY, varName.append(matchedChar())),
            push(new ConcordionSetNode(varName.getString(), text.getString()))
        );
    }

    public Rule assertEqualsCommand(StringBuilderVar text) {
        StringBuilderVar expression = new StringBuilderVar();
        return NodeSequence(
                "=",
                OneOrMore(TestNot("?"), BaseParser.ANY, expression.append(matchedChar())),
                "?",
                push(new ConcordionEqualsNode(expression.getString(), text.getString()))
        );
    }
    
    public Rule execute1Command(StringBuilderVar text) {
        StringBuilderVar expression = new StringBuilderVar();
        return NodeSequence(
                OneOrMore(TestNot(CLOSING), BaseParser.ANY, expression.append(matchedChar())),
//                "?",
                push(new ConcordionExecuteNode(expression.getString(), text.getString()))
        );
    }
    
    public Rule execute2Command(StringBuilderVar text) {
        StringBuilderVar expression = new StringBuilderVar();
        return NodeSequence(
                OneOrMore(TestNot("`"), BaseParser.ANY, expression.append(matchedChar())),
                push(new ConcordionExecuteNode(expression.getString(), text.getString()))
        );
    }
    
    // c:set: {#x="1"}  {#x="1\"}"}
    // c:assertEquals: {add(#x,#y)=="3"}   {#result=="7"} {greeting=="Hello"}  {getGreeting()=="Hello"}
    // c:execute: {foo()} {#x=foo()} {foo(#TEXT)} {#x=foo(#TEXT)} {foo(#x, #y)} {#z=foo(#x, #y)} {#x=greeting} {foo(#x, "one")}
    // c:verifyRows: {#detail:getDetails()} {#detail: #details} {#detail: details}
}
