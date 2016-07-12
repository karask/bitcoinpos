
# Introduction


## Features
* Uses Yahoo Finance API for conversion rates between currencies
* Uses Blockr API for now (could be easily extended to support other APIs, directly the Bitcoin network or both)

## Notes
* BTC to local currency conversion rate is used both ways. Local currency to BTC differs more so the amount in UI would be different between conversions
* Still due to decimal precision between conversions of the amount there will still be very slight precision variations!
* Requires client's wallet to support BIP 21 URI scheme

## Known Issues
* Support API >= 14?? (depends on the scanning for now)
    - however QR code scanning is supported from API >= 21
    - QR code issues even for >=21 ... either googles fixes or we change to zbar/zxing
* validate address is not working for (???) HD address (different checksum method?)

## TODO
* Request Payment button goes to floating fragment and shows QR code for payment (check BIPs!) and updated exchangeRates (if connection available!)
* use zxing to display QR code image (and then zxing to scan QR codes as well ?????
    - http://stackoverflow.com/questions/18543668/integrate-zxing-in-android-studio
    
