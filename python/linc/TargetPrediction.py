# -*- coding: UTF-8 -*-
import pandas as pd
from pandas import DataFrame, Series
import numpy as np

class TargetPrediction:
    # 读取相关数据集
    def file2dataframe(self):
        dir = "C:\\Users\\wyq\\Desktop\\WikiDataAnalyse\\data\\target_prediction\\"
        links = pd.read_csv(dir + 'links.tsv', sep='\t', header=None)
        paths = pd.read_csv(dir + 'paths_finished.tsv', sep='\t')
        paths["path"] = paths["path"].apply(lambda x: x.split(';'))
        vectors = self.normalize()
        self.links = links
        self.paths = paths
        self.vectors = vectors
        return links, paths, vectors

    # 归一化
    def normalize(self):
        dir = "C:\\Users\\wyq\\Desktop\\WikiDataAnalyse\\data\\target_prediction\\"
        vectsWithName = pd.read_csv(dir + 'vector_list_with_name.txt', sep="\\s+", header=None)
        vects = vectsWithName.iloc[:,0:50]
        vectsNorm = (vects - vects.mean())/(vects.max() - vects.min())
        return vectsNorm.set_index(vectsWithName[50].apply(lambda x: x[:-4]))

    # 生成特征向量 u1, u2, t为向量, deg1, deg2为数值
    def feature(self, u1, u2, t, deg1, deg2):
        # print u1, u2, t
        f1 = ((u2 - t) ** 2).sum()
        f2 = ((u1 - u2) ** 2).sum()
        deg1 = (deg1 - 26.135165) / 293
        deg2 = (deg2 - 26.135165) / 293
        f3 = deg2
        f4 = deg1 * deg2
        return Series([f1, f2, f3, f4])

    # 计算P(u2|u1,t;theta) * f
    def p(self, u1, v, target, theta):
        links = self.links
        vectors = self.vectors
        us = links[links[0] == u1][1].values
        fm = 0
        for j in range(us.size):
            u2 = us[j]
            deg2 = links[links[0] == u2][1].values
            f = self.feature(vectors.ix[u1], vectors.ix[u2], vectors.ix[target], len(us), len(deg2))
            if u2 == v:
                fz = np.exp((theta * f).sum())
            # print theta
            # print f
            fm = fm + np.exp((theta * f).sum())
        deg2 = links[links[0] == v][1].values
        f = self.feature(vectors.ix[u1], vectors.ix[v], vectors.ix[target], len(us), len(deg2))
        return (fz / fm) * f

    # 计算P(q|t; theta)
    def probability(self, prefix, target, theta, k=2):
        links = self.links
        vectors = self.vectors
        p = 1.0 / 4604
        for i in range(k-1):
            fz = 0
            fm = 0
            u1 = prefix[i]
            us = links[links[0] == u1][1].values
            for j in range(len(us)):
                u2 = us[j]
                deg2 = links[links[0] == u2][1].values
                f = self.feature(vectors.ix[u1], vectors.ix[u2], vectors.ix[target], len(us), len(deg2))
                if u2 == prefix[i+1]:
                    fz = np.exp((theta[i] * f).sum())
                fm = fm + np.exp((theta[i] * f).sum())
            p = p * (fz / fm)
        return p

    # 排序
    def rank(self, prefix, target, theta, k=2):
        # links = self.links
        vectors = self.vectors
        res = DataFrame(columns=["p"], index=vectors.index)
        for i in range(vectors.shape[0]):
            t = vectors.index[i]
            flag = False
            for j in range(k):
                if t == prefix[j]:
                    flag = True
            if flag == False:
                res.loc[t] = self.probability(prefix, t, theta, k)
                # print res.loc[t]
        r = res.rank(ascending=False)['p'][target]
        return r

    # 代价函数
    def cost(self, theta, k=2):
        # links = self.links
        paths = self.paths
        # vectors = self.vectors
        cnt = 0
        for i in range(1):         # 对于每一个 path   len(paths["path"])
            l = len(paths["path"][i])
            if l > k:
                r = self.rank(paths["path"][i], paths["path"][i][l-1], theta, k)
                print "rank: %d"%r
                cnt += 1/r
            else:
                print "path长度小于%d，忽略继续"%k
        return cnt

    # 训练
    def training(self, k=2, iters=1, alpha=1, randomNum=10):
        links, paths, vectors = self.file2dataframe()
        theta = np.ones((k-1, 4))
        for cnt in range(iters):                    # 迭代次数
            for i in range(randomNum):         # 对于每一个样本前缀 i (path)  paths.shape[0]
                for j in range(k-1):                # 每一个 path 的第 j 次点击
                    prefix = paths['path'][i]
                    # print prefix
                    u1 = prefix[j]
                    u2 = prefix[j+1]
                    t = prefix[len(prefix)-1]
                    us = links[links[0] == u1][1].values
                    deg2 = links[links[0] == u2][1].values
                    # print u1, u2, t
                    # print(vectors)
                    f = self.feature(vectors.ix[u1], vectors.ix[u2], vectors.ix[t], len(us), len(deg2))
                    sum = np.zeros(4)
                    for v in range(len(us)):
                        # print(sum)
                        # print(p(u1, u2, t, links, vectors, theta[j]))
                        sum = sum + self.p(u1, u2, t, theta[j])
                    theta[j] = theta[j] + alpha*(f - sum)
        return theta

    # run
    def run(self, randomNum):
        theta = self.training(randomNum=randomNum)
        print theta
        self.file2dataframe()
        self.cost(theta, k=2)

tp = TargetPrediction()
print "started"
tp.run(1)