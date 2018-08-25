#!/usr/bin/env python3
import argparse
import json

parser = argparse.ArgumentParser()
parser.add_argument('input')
parser.add_argument('output')

args = parser.parse_args()

with open(args.input, "r") as f:
    definition = json.load(f)

definition["types"] = [
    {
        "name": typename,
        "def": typedef
    }
    for typename, typedef in definition["types"].items()
]

with open(args.output, "w") as f:
    json.dump(definition, f, ensure_ascii=False, indent=4)
