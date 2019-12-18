<!DOCTYPE html>
<html>
<head>
    <meta charset="utf‐8">
    <title>Hello World!</title>
</head>
<body>
Hello ${name}!
<br>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>金额</td>
        <#--        <td>出生日期</td>-->
    </tr>

    <#if stus??>
        <#list stus as stu>
            <tr>
                <td>${stu_index}</td>
                <td>${stu.name}</td>
                <td>${stu.age}</td>
                <td <#if (stu.money>300)>style="color: red" </#if>>${stu.money}</td>
                <td>${stu.birthday?date}</td>
            </tr>
        </#list>
        <br>
        学生的个数: ${stus?size}
        <br>
        ${i?c}
    </#if>

</table>
<br>
<p>map 的取值</p>
姓名:${(stuMap['stu1'].name)!""}<br>
年龄:${(stuMap['stu1'].age)!""}<br>
姓名:${stuMap.stu2.name}<br>
年龄:${stuMap.stu2.age}<br>
<p>map 遍历的取值</p>

<#list stuMap?keys as k>
    姓名:${stuMap[k].name}<br>
    年龄:${stuMap[k].age}<br>
</#list>


</body>
</html>