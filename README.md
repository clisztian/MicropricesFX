# MicropricesFX
Based on the microprice work of Stoikov (https://papers.ssrn.com/sol3/papers.cfm?abstract_id=2970694) we extend the microprice estimator to various estimators on the orderbook to account for withdraw rates and higher price levels in the orderbook. 

The idea is that large orders on the bid or ask side can also influence the microprice without the best bid ask or spread every changing. This leads to good indicators for spoofing/layering: large deviations in microprice can possibly be the after-effects of "bluffing" in the orderbook. 

To compute the adjusted microprice, we propose a simple error-correcting model for a high-frequency estimator of
future prices given higher order information of imbalances in the orderbook. The model takes into
account a current microprice estimate given the spread and best bid to ask imbalance, and adjusts the
micro-price based on recent dynamics of higher price rank imbalances. This code introduces a computationally
fast estimator using a recently proposed hyperdimensional vector Tsetlin machine framework.

![image](https://github.com/user-attachments/assets/fbc1383f-c3c0-4eaf-a292-9a1beaf0769d)
