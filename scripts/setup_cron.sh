#!/bin/bash
# প্রতিদিন সকাল ৮টায় auto update চলবে
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CRON_CMD="0 8 * * * cd $SCRIPT_DIR/.. && python3 scripts/auto_price_updater.py >> scripts/price_update.log 2>&1"

# Cron এ যোগ করা
(crontab -l 2>/dev/null | grep -v "auto_price_updater"; echo "$CRON_CMD") | crontab -
echo "✅ Cron job set! প্রতিদিন সকাল ৮টায় দাম আপডেট হবে।"
crontab -l
