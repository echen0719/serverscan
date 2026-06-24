## Overview

Server Scan is a Minecraft mod that runs network scans to find online IP addresses. The scanner will find servers and save that as a JSON file. There is a built-in explorer for past scans and an additional explorer to quickly add discovered servers to your Minecraft server list. The mod works on Windows and Linux (with the driver/permission notes below).

## Usage

- Run scans: Enter IP ranges (IPv4), port ranges, speed (packets per second), batch size, and output filename, then click "Run Scan".
- Pause/Resume: Click "Pause" to finish the current chunk and merge; change rate/batch while paused and click "Resume".
- Stop: Click "Stop" to terminate and merge completed chunks.
- Past results: Search, rename, delete, or view saved JSON results.
- File/Server explorers: GUI interface to access and modify output files along with viewing found server IPs.
- Preferred values: On 1 GiB/s interfaces, 1.2 Mpps (1,200,000 pps) at ~3,000,000 batch size is reasonable.
  
### Dependencies

- On Windows, install [Npcap](https://npcap.com/#download) (over the deprecated WinPcap)
- On Linux, install masscan via your package manager and grant raw network capabilities

```bash
# Debian-based
apt install masscan

# Fedora/RHEL
dnf install masscan

# Arch Linux-based
pacman -S masscan

# install to /usr/bin/masscan or /usr/local/bin/masscan

# Give permissions to masscan
setcap cap_net_raw=ep $(which masscan)
```

If you want to remove permissions to masscan

```bash
setcap -r $(which masscan)
```

**Note:** If scanning does not happen in the mod due to permission errors, run Minecraft with administrative privileges.

On both operating systems, make sure to install Fabric API valid for the Server Scan version. Restart computer if necessary.

## Features

- Masscan implementation: Uses the masscan binary (by [https://github.com/robertdavidgraham/masscan](https://github.com/robertdavidgraham/masscan)).
- High speed scanning: Supports large rates and batch sizes (capable of millions of packets per second). Actual max speeds depends on drive and networks speeds.
- Chunking & batching: IP ranges are split into queueable chunks. Each chunk is scanned separately and merged for a final output.
- Pause / Stop handling: Pause waits for the active chunk to finish and merges progress. Stop attempts graceful shutdown and force kills if needed.
- Live logs: Live log window to view the output of masscan in case anything goes wrong.
- JSON format: Writes the IP addresses in JSON format for easy processing.

### Note to player:

This is only the first iteration of this mod. If there are any issues or suggesstion, please add them to the GitHub [https://github.com/echen0719/serverscan/issues](https://github.com/echen0719/serverscan/issues) so I can improve this mod (with a label please).

## Credits

Author: @echen0719 ([GitHub](https://www.github.com/echen0719))

Masscan: @robertdavidgraham ([GitHub](https://github.com/robertdavidgraham))
