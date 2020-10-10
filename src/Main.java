import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Main {

    public static void main(String[] args) throws IllegalArgumentException, NoSuchElementException
    {
        String hex = toHex(12500);
        if (hex.equalsIgnoreCase("30D4")) {
            System.out.println("测试成功");
        } else {
            System.out.println("测试失败");
        }

        String exp = "1 + 2 * (9 - 5)";
        SuffixExpression se = compile(exp);
        int result = se.execute();
        System.out.println(exp + " = " + result + " " + (result == 1 + 2 * (9 - 5) ? "✓" : "✗"));
    }

    public static String toHex(int n)
    {

        Deque<String> q = new LinkedList<>();
        while (n != 0) {
            int sh  = n % 16;
            n = n / 16;
            q.push(Integer.toHexString(sh));
        }

        StringBuilder hex = new StringBuilder();
        while (!q.isEmpty()) {
            hex.append(q.pop());
        }
        return hex.toString();
    }

    static SuffixExpression compile(String exp) {
        //定义运算符的优先级别
        Map<String, Integer> expPriority = Map.of(
                "+", 1,
                "-", 1,
                "*", 2,
                "/", 2,
                "(", 0,
                ")", 0
        );

        //将表达式的数值和运算符通过正则匹配出来
        String expReg = "\\d+|[\\+\\-\\*\\/\\(\\)]";
        Matcher expMatcher = Pattern.compile(expReg).matcher(exp);

        //定义后缀表达式栈和运算符栈
        //后缀表达式栈存储最终得到的后缀表达式栈
        //运算符栈存储算法过程中，优先级为不存在括号等改变优先级别的运算符
        //优先级别一旦别改变，则将符号转移到后缀表达式栈
        Deque<String> suffixExpStack = new LinkedList<>();
        Deque<String> operateStack = new LinkedList<>();
        while (expMatcher.find()) {
            String current = expMatcher.group();

            //数值直接存入后缀表达式栈
            //并进入下一次循环
            if (!expPriority.containsKey(current)) {
                suffixExpStack.push(current);
                continue;
            }

            //如果是左括号，或操作符栈为空
            // 则将当前运算符存入运算符栈
            //作为在右括号前的所有符号标识
            if (current.equalsIgnoreCase("(") || operateStack.isEmpty()) {
                operateStack.push(current);
                continue;
            }

            //如果遇到右括号，则将操作符栈的操作符弹出
            //压入后缀表达式栈，直到遇到操作符栈的左括号标识
            if (current.equalsIgnoreCase(")")) {
                String temp = operateStack.pop();
                while (temp != null && !temp.equalsIgnoreCase("(")) {
                    suffixExpStack.push(temp);
                    temp = operateStack.pop();
                }
                continue;
            }

            //如果是非左括号和右括号的运算符
            //比较运算符栈的栈顶运算符优先级别与当前优先级别
            //如果当前优先级别较高，则可以压入操作符栈
            //如果当前优先级别较低，则压入后缀表达式栈
            String topOperate = operateStack.peek();
            if (expPriority.get(topOperate) <= expPriority.get(current)) {
                operateStack.push(current);
            } else {
                topOperate = operateStack.pop();
                operateStack.push(current);
                suffixExpStack.push(topOperate);
            }

        }

        //将运算符栈剩余的运算符压入后缀表达式
        while (!operateStack.isEmpty()) {
            suffixExpStack.push(operateStack.pop());
        }

        return new SuffixExpression(suffixExpStack);
    }
}

class SuffixExpression {
    Deque<? extends  String> suffix;
    public SuffixExpression (Deque<? extends String> suffix) {
        this.suffix = suffix;
    }

    public int execute()
    {
        Deque<Integer> resultStack = new LinkedList<>();
        while (!this.suffix.isEmpty()) {

            //因为后缀表达式压栈是从中缀表达式头部到尾部
            //所以计算需要从栈底开始取值
            String value = this.suffix.pollLast();

            //如果是数字，直接压入结果栈
            if (value.matches("^[0-9]+$")) {
                resultStack.push(Integer.parseInt(value));
                continue;
            }

            //运算符，则取栈顶两个元素进行运算
            int val1 = resultStack.pop();
            int val2 = resultStack.pop();
            resultStack.push(operate(val2, val1, value));
        }

        return resultStack.pop();
    }

    public int operate(int val1, int val2, String operate)
    {
        switch (operate) {
            case "+" : return val1 + val2;
            case "-" : return val1 - val2;
            case "*" : return val1 * val2;
            case "/" : return val1 / val2;
            default : throw new IllegalArgumentException("运算符不正确!");
        }
    }
}