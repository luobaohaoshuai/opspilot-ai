#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""Embedding 工具（智谱AI）：读取文本列表，输出 JSON 格式的向量数组。
用法：echo '["文本1","文本2"]' | python embed.py
"""
import sys
import json
import os
from openai import OpenAI

api_key = os.environ.get('ZHIPU_API_KEY')
if not api_key:
    raise RuntimeError('Missing required environment variable: ZHIPU_API_KEY')

client = OpenAI(
    api_key=api_key,
    base_url='https://open.bigmodel.cn/api/paas/v4'
)

texts = json.loads(sys.stdin.read())
resp = client.embeddings.create(model='embedding-3', input=texts)

result = [{"text": texts[i], "embedding": d.embedding}
          for i, d in enumerate(resp.data)]
print(json.dumps(result, ensure_ascii=False))
