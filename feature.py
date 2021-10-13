import os
import pandas as pd
import scipy.stats as stats
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.linear_model import LogisticRegression

def jaccard_similarity(x, y):

    intersection_cardinality = len(set.intersection(*[set(x), set(y)]))
    union_cardinality = len(set.union(*[set(x), set(y)]))
    return intersection_cardinality / float(union_cardinality)


def tfidf_cosine_similarity(x, y):
    docx = " ".join(set(x))
    docy = " ".join(set(y))
    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform([docx.lower(), docy.lower()])
    result = cosine_similarity(tfidf_matrix[0], tfidf_matrix[1])[0][0]
    return result

def mann_whitney_test(x, y):
    x = get_distribution(x)
    y = get_distribution(y)
    if len(x) > 1 and len(y) > 1:
        if x[-1] != 0 and y[-1] != 0:
            result = stats.mannwhitneyu(x, y)[1]
            return result

    return 0


def get_freq_dict(data):
    freq_dict = {}.fromkeys(data, 0)
    for ele in data:
        freq_dict[ele] += 1

    return freq_dict


def get_distribution(data):
    freq_dict = get_freq_dict(data)
    histogram_list = []
    i = 0
    for v in reversed(sorted(freq_dict.values())):
        for _ in range(int(v*100/len(data))):
            histogram_list.append(i)
        i += 1
    return histogram_list

if __name__ == '__main__':
    os.chdir("D:\\kg_20210906\\sources-modified-20210828")
    s01 = pd.read_csv('s01.csv')
    s02 = pd.read_csv('s02.csv')
    s03 = pd.read_csv('s03.csv')
    s04 = pd.read_csv('s04.csv')
    s05 = pd.read_csv('s05.csv')
    s06 = pd.read_csv('s06.csv', encoding='ansi')
    s07 = pd.read_csv('s07.csv')
    s08 = pd.read_csv('s08.csv', encoding='ansi')
    s09 = pd.read_csv('s09.csv')
    s10 = pd.read_csv('s10.csv')
    s11 = pd.read_csv('s11.csv')
    s12 = pd.read_csv('s12.csv', encoding='ansi')
    s13 = pd.read_csv('s13.csv', encoding='ansi')
    s14 = pd.read_csv('s14.csv')
    s15 = pd.read_csv('s15.csv')
    s16 = pd.read_csv('s16.csv')
    s17 = pd.read_csv('s17.csv')
    s18 = pd.read_csv('s18.csv')
    s19 = pd.read_csv('s19.csv')
    s20 = pd.read_csv('s20.csv')
    s21 = pd.read_csv('s21.csv', encoding='ansi')
    s22 = pd.read_csv('s22.csv')
    s23 = pd.read_csv('s23.csv', encoding='ansi')
    s24 = pd.read_csv('s24.csv')
    s25 = pd.read_csv('s25.csv')
    s26 = pd.read_csv('s26.csv')
    s28 = pd.read_csv('s28.csv')
    s29 = pd.read_csv('s29.csv')

