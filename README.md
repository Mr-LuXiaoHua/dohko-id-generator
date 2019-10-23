dohko-id-generator
---
Snowflake雪花算法生成器，单例模式.

程序中固定一个数据库中心，使用机器ip作为workerId.

使用方法：
```
long id = IdGenerator.generateId()
```