# Pattern Mining

关于模式挖掘项目的相关算法实现

源码和数据集都已(或即将)公布到 数据挖掘开源库 [SPMF](http://www.philippe-fournier-viger.com/spmf/) 上 



## 1. Periodic Pattern （周期模式）

频繁项集挖掘是数据分析里很重要的任务。 同时作为一种无监督学习的方法，在关联规则挖掘、序列挖掘、聚类和分类等任务中 都有应用场景。虽然，频繁项集的挖掘很有用，但是 仅仅只是频繁的话( 支持度高于某一阈值 )会输出大量的频繁模式， 如果对这些模式进行分析的话也会消耗大量的时间精力。

周期模式挖掘就是解决这些问题的一个方向，通过限制 用户(或项集) 出现的行为 来挖掘 具有周期效应的 模式。

最初对**周期的定义**是  **用户(或项集) 出现的相邻时间点的最大差**  

这种简单的定义存在很大的缺陷，之后有很多学者对其进行很多改进或融合或应用。更多信息可 浏览 [SPMF](http://www.philippe-fournier-viger.com/spmf/index.php?link=algorithms.php)

以下是我的几篇论文里算法的简单介绍与实现



### 1.1 [SPP-Growth](SPP-Growth)

相关论文：

Fournier-Viger, P., **Yang, P.**, Lin, J. C.-W., Kiran, U. (2019). **Discovering Stable Periodic-Frequent Patterns in Transactional Data**. Proc. 32nd Intern. Conf. on Industrial, Engineering and Other Applications of Applied Intelligent Systems (IEA AIE 2019), Springer LNAI, 14 pages (to appear)

主要贡献：

- 对现有的周期测量方法进行讨论，总结出不足的共同点，并依此提出了一个新颖的周期测量方法
- 设计了基于字典树的数据结构来降低数据的访问次数和内存消耗
- 基于[FP-Growth](http://www.philippe-fournier-viger.com/spmf/index.php?link=algorithms.php)提出了不会产生候选项集的高效算法



### 1.2 [PFPM](http://www.philippe-fournier-viger.com/spmf/index.php?link=algorithms.php)

相关论文：

Fournier-Viger, P., **Yang, P.**, Lin, J. C.-W, Duong, Q.-H., Dam, T.-L., Sevcik, L., Uhrin, D., Voznak, M. (2019). **Discovering Periodic Itemsets using Novel Periodicity Measures**. Advances in Electrical and Electronic Engineering (to appear)

主要贡献：

-  提出了更加合适、灵活的周期测量方法和对应的剪枝策略(闭包属性)
-  引入支持度联系结构，在过程中进行剪枝，加快算法运行
-  提出了基于[ECLAT](http://www.philippe-fournier-viger.com/spmf/index.php?link=algorithms.php)的高效算法来正确挖掘该模式



### 1.3 [MRCPPS](MRCPPS)

相关论文:

Fournier-Viger, P., **Yang, P.**,  Li, Z., Lin, J. C.-W, Kiran, U. (2019). **Discovering Rare Correlated Periodic Patterns in Multiple Sequences**. (a submitted journal paper)

主要贡献:

- 定义了一个在多序列中**挖掘稀有周期模式**的新问题和对应的新的周期测量方法

-  学习并讨论了相关测量的属性，并提出了一个上界用来减少候选项集的产生

-  设计了一种新颖的结构来降低数据访问次数，并依此实现了高效的算法

  



### 1.4 [$LPPM_{breadth}$](.) 、 [$LPPM_{depth}$](.)、[$LPP\text{-}Growth$](.) 

相关论文:

**Discovering Locally Periodic Patterns in a Discrete Sequence** (Writing in progress)

主要贡献:

​	定义了一种新的模式--局部周期模式 以及对应的 周期时间区间 来更完整精确的定位周期行为

​	LPPM利用二进制垂直数据库的表示形式，通过共享前缀的方式进行深度优先和广度优先进行搜索

​	LPP-Growth 利用 compact tree 来无损的保存数据信息，并从下往上不断递推条件树来快速挖掘





## 2. Episode Mining （片段挖掘）

Episode Mining 是作用在 一条长 的序列数据中， 并从中挖掘 人们感兴趣的片段。

应用场景: 电信网络的报警序列、时间错误报告序列、网络浏览序列、文本、股票序列、交通数据 等



### 2.1 [MINEPI](EpisodeMining)   

相关论文：

Mannila, H. , Toivonen, H. , & Verkamo, A. I. . (1997). **Discovery of frequent episodes in event sequences**. Data Mining and Knowledge Discovery, 1(3), 259-289.

我的实现:

​	主要对论文中 Serial Episode 的挖掘进行了实现， 按照论文的方法，采用 有限状态自动机 的方法 来识别 episode 和 minimal occurrence。



### 2.2 [MINEPI+](EpisodeMining) 、 [EMMA](EpisodeMining)

相关论文：

Huang, K. Y. , & Chang, C. H. . (2008). **Efficient mining of frequent episodes from complex sequences**. Information Systems, *33*(1), 96-114.

我的实现:

​	对论文中 提出新的支持度方法 (head frequency) 进行了实现，并 对论文中提出来的 两种方法 **MINEPI+** 和 **EMMA** 进行了实现。 同时 按照论文的想法 引入到 更加复杂的序列当中 ( 每个时间点可以具有多个事件 )。

