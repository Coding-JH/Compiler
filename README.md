关键字（Keyword）
符号（Sign）
标示符（Identifier）:^[a-zA-Z_][0-9a-zA-Z_]*[!\?]?$
##数据类型
>数字类型（Number）
>字符串（String）
>正则表达式（RegEx）
>终止符（EndSymbol）

当源代码读完了，如果状态机处于Normal状态，此时应该生成一个EndSymbol
###对于非 Normal 状态，我只需要关心两个过程：
>1.何时从 Normal 跳转到该状态；
>2.何时从该状态跳回 Normal 状态。