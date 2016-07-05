
# Introduction


## Features
* Uses Yahoo Finance API for conversion rates between currencies

## Notes
* BTC to local currency conversion rate is used both ways. Local currency to BTC differs a bit more so the amount in UI would be confusing for users
* Still due to decimal precision of the amount there might still be very slight precision variations when converting from and to an specific amount

## Known Issues
* Support API >= 14?? (depends on the scanning for now)
    - however QR code scanning is supported from API >= 21
    - QR code issues even for >=21 ... either googles fixes or we change to zbar/zxing

## TODO

