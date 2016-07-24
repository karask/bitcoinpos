
# Introduction


## Features
* Supports API version >= 15
* Supports BIP 21 URI scheme
* Uses Yahoo Finance API for conversion rates between currencies
* Uses Blockr API for now (could be easily extended to support other APIs, directly the Bitcoin network or both)

## Notes
* BTC to local currency conversion rate is used both ways. Local currency to BTC differs more so the amount in UI would be different between conversions
* Still due to decimal precision between conversions of the amount there will still be very slight precision variations!
* Requires client's wallet to support BIP 21 URI scheme

## Known Issues


## TODO    
* Test with multisig accounts
* Test with HD addresses
* Clean code 
* Make transaction history dates more user friendly (e.g. a few seconds ago, an hour ago, etc.)