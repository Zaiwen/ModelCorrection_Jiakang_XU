import os
import sys

import pandas as pd
import feature
import random
from sklearn.linear_model import LogisticRegression

class Column:
    def __init__(self, value_list, semantic_type=None, source_name=None):
        self.value_list = value_list
        self.is_labeled = False
        self.semantic_type = semantic_type
        self.source_name = source_name


    def __repr__(self):
        return self.value_list.__repr__()+ "\n" + "SemanticType:"+self.semantic_type

class Features:

    def __init__(self, col_a:Column, col_b:Column):
        self.col_a = col_a
        self.col_b = col_b
        self.fs = {}
        self.label = None
        self.get_features()

    def get_features(self):
        self.fs["jaccard"] = feature.jaccard_similarity(self.col_a.value_list, self.col_b.value_list)
        self.fs["TF-IDF"] = feature.tfidf_cosine_similarity(self.col_a.value_list, self.col_b.value_list)
        self.fs["MW"] = feature.mann_whitney_test(self.col_a.value_list, self.col_b.value_list)

    def set_label(self, label):
        self.label = label


    def to_data_frame(self):
        df = pd.DataFrame([self.fs])
        df = df.join(pd.DataFrame([{"label":self.label}]))
        df = df.join(pd.DataFrame([{"ca":self.col_a.source_name, "cb":self.col_b.source_name}]))
        df["ca_type"] = [self.col_a.semantic_type]
        df["cb_type"] = [self.col_b.semantic_type]
        return df


    def __repr__(self):
        return self.fs.__repr__()


def train(train_columns):

    ncol = len(train_columns)
    train_features = []
    for i in range(ncol):
        for j in range(i+1, ncol):
            fv = Features(train_columns[i], train_columns[j])
            fv.set_label(1 if train_columns[i].semantic_type == train_columns[j].semantic_type else 0)
            train_features.append(fv)

    return train_features

def test(train_columns, test_column):
    test_features = []
    for train_col in train_columns:
        fv = Features(test_column, train_col)
        test_features.append(fv)
    return test_features


def load_all_columns():
    cols = []
    for file in os.listdir(r"D:\kg_20210906\trains"):
        source_name = file[:file.rfind('_')]
        semantic_type = file[file.rfind('_')+1:-4]
        with open(r"D:\kg_20210906\trains\\"+file, 'r')as f:
            ls = [line.strip() for line in f]
            # print(ls)
        cols.append(Column(ls, semantic_type, source_name))

    return cols






if __name__ == '__main__':
    cols = load_all_columns()
    for i in range(len(cols)-1, -1, -1):
        if len(cols[i].value_list) < 40:
            cols.remove(cols[i])

    j = 5

    type1_cols = [col for col in cols if col.semantic_type == "type1"]
    type2_cols = [col for col in cols if col.semantic_type == "type2"]


    os.chdir(f"D:/kg_20210906/exp_20210928/exp_{j}")

    random.seed(random.seed(i))
    train_cols = []
    train_cols.extend(random.sample(type1_cols, 6))
    train_cols.extend(random.sample(type2_cols, 3))
    test_cols = [col for col in cols if col not in train_cols]

    train_fvs = train(train_cols)
    # train_fvs = train(cols)
    train_df = train_fvs[0].to_data_frame()
    for i in range(1, len(train_fvs)):
        train_df = train_df.append(train_fvs[i].to_data_frame())

    # print(train_df)
    train_df.to_csv("train.csv", index=False)
    lr = LogisticRegression(class_weight="balanced")
    lr.fit(train_df[['jaccard', 'TF-IDF','MW']], train_df['label'])


    result_df = pd.DataFrame(columns=['source', 'predict_type', 'real_type'])


    for test_col in test_cols:
        test_fvs = test(train_cols, test_col)

        x_test = pd.DataFrame([fv.fs for fv in test_fvs])
        predict_values = lr.predict(x_test)

        x_test['predict'] = predict_values
        x_test['train'] = [fv.col_b.source_name for fv in test_fvs]
        x_test['test'] = [fv.col_a.source_name for fv in test_fvs]
        x_test['train_type'] = [fv.col_b.semantic_type for fv in test_fvs]
        x_test['test_type'] = [fv.col_a.semantic_type for fv in test_fvs]
        predict_types = []
        for i in range(len(predict_values)):
            if predict_values[i] == 1:
                predict_types.append(test_fvs[i].col_b.semantic_type)
            else:
                predict_types.append("type1" if test_fvs[i].col_b.semantic_type == "type2" else "type2")
        x_test['predict_types'] = predict_types

        x_test.to_csv(f"test__{test_col.source_name}__.csv", index=False)
        source = test_col.source_name
        predict_type = 'type1' if predict_types.count('type1') > predict_types.count('type2') else 'type2'
        real_type = test_col.semantic_type

        result_df = result_df.append({'source':source, 'predict_type':predict_type, 'real_type':real_type}, ignore_index=True)


    result_df.to_csv(f"result{j}.csv", index=False)


