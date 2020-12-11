# lod-recsys

Lanciare la classe UserItemPriorGraph per eseguire il Page Rank personalizzato.
Nel main settare i path dei file di train e test; settare il path e il nome del file di output (PrintWriter pw).
Si raccomanda di utilizzare file .tsv così costruiti: IDuser\tIDitem (test), IDuser\tIDitem\tRating (train; rating binario).
Date le impostazioni, si consiglia di produrre in output sempre un file .tsv.
Se si vuole eseguire la variante classica del Page Rank: nel main sostituire 0.85 con 1.0 (riga 140), in profileUser decommentare riga 114 e commentare riga 112.

Lanciare la classe UserItemPropLodPrior per eseguire il Page Rank personalizzato con proprietà.
Settare il nome del file delle proprietà (propFile, riga 34; utilizzare un file tsv così costruito: IDitem\tProperty).
Si può scegliere tra tre varianti già predisposte di proprietà; scegliere quella che interessa e commentare le altre due.
Come per la casse precedente, settare nel main i path di train, test e output (indicazioni analoghe riguardanti il formato).
