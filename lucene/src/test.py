stopwords_options = [False, True]
stemming_options = ["no stemming","porter","krovetz"]
model_options = ["VSM", "BM25", "LM"]

stopwords = "a"
VSM = 1
BM25 = 2
LM = 3

stopwords_dict = {False: None , True: "a"}
model_options_dict = {"VSM": VSM , "BM25": BM25, "LM": LM}

stopwords_display_dict = {False: "No_stopwords", True: "Stopwords"}
stemming_display_dict = {"no stemming": "No_stemming", "porter": "Porter_stemming", "krovetz": "Krovetz_stemming"}
model_display_dict = {"VSM": "VSM", "BM25": "BM25", "LM": "LM"}

for stemming in stemming_options:
    for stopwords in stopwords_options:
        index_path = f"/mnt/d/yuchenxi/UDEM/diro/IFT6255/devoir1/AP_index_{stemming_display_dict[stemming]}_{stopwords_display_dict[stopwords]}"
        output_path_root = f"/mnt/d/yuchenxi/UDEM/diro/IFT6255/devoir1/AP_ranking{stemming_display_dict[stemming]}_{stopwords_display_dict[stopwords]}"


        for model in model_options:
            output_path = f"{output_path_root}_{model_display_dict[model]}.txt"
            print(f"Running {model} model with {stopwords_display_dict[stopwords]} and {stemming_display_dict[stemming]}...")
            print("index_path:")
            print(index_path)
            print("output_path:")
            print(output_path)
