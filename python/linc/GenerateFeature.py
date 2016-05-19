# -*- coding: UTF-8 -*-
import pandas as pd
from pandas import DataFrame, Series
import numpy as np

# 读取相关数据集
def file2dataframe():
    dir = "C:\\Users\\wyq\\Desktop\\WikiDataAnalyse\\data\\target_prediction\\"
    links = pd.read_csv(dir + 'links.tsv', sep='\t', header=None)
    paths = pd.read_csv(dir + 'paths_finished.tsv', sep='\t')
    paths["path"] = paths["path"].apply(lambda x: x.split(';'))
    vectors = normalize()
    return links, paths, vectors

# 归一化
def normalize():
    dir = "C:\\Users\\wyq\\Desktop\\WikiDataAnalyse\\data\\target_prediction\\"
    vectsWithName = pd.read_csv(dir + 'vector_list_with_name.txt', sep="\\s+", header=None)
    vects = vectsWithName.iloc[:,0:50]
    vectsNorm = (vects - vects.mean())/(vects.max() - vects.min())
    return vectsNorm.set_index(vectsWithName[50].apply(lambda x: x[:-4]))

# 生成特征向量 u1, u2, t为向量, deg1, deg2为数值
def feature(u1, u2, t, deg1, deg2):
    # print u1, u2, t
    f1 = ((u2 - t) ** 2).sum()
    f2 = ((u1 - u2) ** 2).sum()
    deg1 = (deg1 - 26.135165) / 293
    deg2 = (deg2 - 26.135165) / 293
    f3 = deg2
    f4 = deg1 * deg2
    return Series([f1, f2, f3, f4])


links, paths, vectors = file2dataframe()

l = len(links[0])
dict = {}
degrees = links[1].groupby(links[0]).count()
for i in range(10):
    u1 = links[0][i]
    u2 = links[1][i]
    cnt = 0
    for t in vectors.index:
        f = feature(vectors.ix[u1], vectors.ix[u2], vectors.ix[t], degrees[u1], degrees[u2])
        dict.update({u1+";"+u2+";"+t : f})
        cnt = cnt + 1
        if cnt == 3:
            break
print len(dict)