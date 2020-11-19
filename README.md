# simune

A simulate network element with configurable rules and messages. Support multi-manufacturer and multi-model equipment (such as Huawei iManager U2000), which can be configured and acted as a multi-manufacturer ems system. It supports the full features of TL1 protocol.

交互规则和报文可配置的模拟网元

运行时的文件目录结构：

│  application.properties  
│  simune-1.0.0.jar│  
└─ conf  
    └─neXX  
    
    ne.xml配置此网元的信息以及此网元全局性的配置	
    cmd.xml中配置业务命令和返回报文， request标签配置请求报文
	response配置返回报文，当response报文来自于一个文件时，resfile可以配置文件名。
	当response和resfile同时非空时，以resfile优先。