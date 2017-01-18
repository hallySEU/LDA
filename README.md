# LDA
LDA实现代码（gibbs sampling），附设计，实验文档（2015），分词工具采用IKAnalyzer

输入--------------------------------

文档集合：目录为data/originalDocs

停用词表：目录为data/stopwords


输出--------------------------------

lda_final.phi：主题下的词分布（K*V）维

lda_final.theta：文档的主题分布（D*K）维

lda_final.topwords：主题下的Top词（按照概率排名）

lda_final.topic_assignment：最后一轮文档中词所分配的主题
