# MicropricesFX
Based on the microprice work of Stoikov (https://papers.ssrn.com/sol3/papers.cfm?abstract_id=2970694) we extend the microprice estimator to various estimators on the orderbook to account for withdraw probability and higher price levels in the orderbook. 

The idea is that large orders on the bid or ask side can also influence the microprice without the best bid ask or spread every changing. This leads to good indicators for spoofing/layering: large deviations in microprice can possibly be the after-effects of "bluffing" in the orderbook. 
