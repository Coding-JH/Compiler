package com;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Mr.Hou on 2019/5/16.
 */
//词法分析器
public class LexicalAnalysis {
    //状态机状态
    private static enum State{
        Normal,
        Identifier,Sign,Annotation,
        String,RegEx,Space;
    }
    public LexicalAnalysis(Reader reader){

    }
    //进行单词化（Tokenization）
    Token read()throws IOException, LexicalAnalysisException{
        return null;
    }

    private final LinkedList<Token> tokenBuffer=new LinkedList<>();

    private StringBuilder readBuffer = null;

    private static final char[] IdentifierRearSign = new char[] {'?', '!'};
    //用于处理转义符号“\”
    private boolean transferredMeaningSign;
    private static final HashMap<Character, Character> StringTMMap = new HashMap<>();
    static {
        StringTMMap.put('\"', '\"');
        StringTMMap.put('\'', '\'');
        StringTMMap.put('\\', '\\');
        StringTMMap.put('b', '\b');
        StringTMMap.put('f', '\f');
        StringTMMap.put('t', '\t');
        StringTMMap.put('r', '\r');
        StringTMMap.put('n', '\n');
    }
    private void refreshBuffer(char c) {
        readBuffer = new StringBuilder();
        readBuffer.append(c);
    }

    private void createToken(Token.Type type) {
        Token token = new Token(type,readBuffer.toString());
        //加入
        tokenBuffer.addFirst(token);
        readBuffer = null;
    }
    /**
     状态机逻辑,转换
     */
    private State state;
    //一个个读入字符
    private boolean readChar(char c) throws LexicalAnalysisException {
        //表示在读完当前 char 之后，方法返回，是否移动游标读取下一个字符。
        //如果为 false，则该函数的下一次调用的参数与前一次调用的参数会一模一样（因为游标没有移动嘛）。
        boolean moveCursor = true;
        Token.Type createType = null;

        if(state == State.Normal) {
            if(inIdentifierSetButNotRear(c)) {
                state = State.Identifier;
            }
            else if(SignParser.inCharSet(c)) {
                state = State.Sign;
            }
            else if(c == '#') {
                state = State.Annotation;
            }
            else if(c == '\"' | c == '\'') {
                state = State.String;
                transferredMeaningSign = false;
            }
            else if(c == '`') {
                state = State.RegEx;
                transferredMeaningSign = false;
            }
            else if(include(Space, c)) {
                state = State.Space;
            }
            else if(c == '\n') {
                createType = Token.Type.NewLine;
            }
            //文件终止
            else if(c == '\0') {
                createType = Token.Type.EndSymbol;
            }
            else {
                throw new LexicalAnalysisException(c);
            }
            refreshBuffer(c);


        } else if(state == State.Identifier) {
            if(inIdentifierSetButNotRear(c)) {
                readBuffer.append(c);
                //'?', '!' 只能用于 Identifier 结尾部分的字符
            } else if(include(IdentifierRearSign, c)) {
                createType = Token.Type.Identifier;
                readBuffer.append(c);
                state = State.Normal;

            } else {
                createType = Token.Type.Identifier;
                state = State.Normal;
                //不移动
                moveCursor = false;
            }
        } else if(state == State.Sign) {

        } else if(state == State.Annotation) {
            //读到换行符（或源代码读完了）则结束
            if(c!='\n' & c!='\0'){
                readBuffer.append(c);
            }else{
                createType = Token.Type.Annotation;
                state = State.Normal;
                //不移动游标,读到的最后一个字符要由跳转回的 Normal 做处理。
                //往往是生成一个 NewLine 类型的 Token 或 EndSymbol 类型的 Token
                moveCursor = false;
            }
        } else if(state == State.String) {
            if(c == '\n') {
                throw new LexicalAnalysisException(c);

            } else if(c == '\0') {
                throw new LexicalAnalysisException(c);

            } else if(transferredMeaningSign) {

                Character tms = StringTMMap.get(c);
                if(tms == null) {
                    throw new LexicalAnalysisException(c);
                }
                readBuffer.append(tms);
                transferredMeaningSign = false;

            } else if(c == '\\') {
                transferredMeaningSign = true;

            } else {
                readBuffer.append(c);
                char firstChar = readBuffer.charAt(0);
                if(firstChar == c) {
                    createType = Token.Type.String;
                    state = State.Normal;
                }
            }
        } else if(state == State.RegEx) {
            if(transferredMeaningSign) {
                //TODO
                if(c != '`') {
                    throw new LexicalAnalysisException(c);
                }
                readBuffer.append(c);
                transferredMeaningSign = false;

            } else if(c =='\\') {
                transferredMeaningSign = true;
            } else if(c == '\0') {
                throw new LexicalAnalysisException(c);

            } else if(c == '`') {
                readBuffer.append(c);
                createType = Token.Type.RegEx;
                state = State.Normal;

            } else {
                readBuffer.append(c);
            }
        } else if(state == State.Space) {
            if(include(Space,c)){
                readBuffer.append(c);
            }else{
                createType= Token.Type.Space;
                state=State.Normal;
                moveCursor=false;
            }
        }
        if(createType != null) {
            createToken(createType);
        }
        return moveCursor;

    }
    private static final char[] Space = new char[] {' ', '\t'};
    //判定字符是否属于标示符字符
    private boolean inIdentifierSetButNotRear(char c) {
        return (c >= 'a' & c <= 'z' ) | (c >='A' & c <= 'Z') | (c >= '0' & c <= '9')|| (c == '_');
    }
    //判断字符是否归属于某个集合
    private boolean include(char[] range, char c) {
        boolean include = false;
        for(int i=0; i<range.length; ++i) {
            if(range[i] == c) {
                include = true;
                break;
            }
        }
        return include;
    }


    private class LexicalAnalysisException extends Exception {
        public LexicalAnalysisException(char c){
            System.out.println();
        }
    }
}
