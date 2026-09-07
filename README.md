## Overview

Server Scan is a Minecraft mod that runs network scans to find online IP addresses. The scanner will find servers and save that as a JSON file. There is a built-in explorer for past scans and an additional explorer to quickly add discovered servers to your Minecraft server list. The mod works on Windows and Linux.

## Usage

- Run scans: Enter IP ranges (IPv4), port ranges, speed (packets per second), batch size, and output filename, then click "Run Scan".
- Pause/Resume: Click "Pause" to finish the current chunk and merge; change rate/batch while paused and click "Resume".
- Stop: Click "Stop" to terminate and merge completed chunks.
- Past results: Search, rename, delete, or view saved JSON results.
- File/Server explorers: GUI interface to access and modify output files along with viewing found server IPs.
- Preferred values: On 1 GiB/s interfaces, 1.2 Mpps (1,200,000 pps) at ~3,000,000 batch size is reasonable.
  
### Dependencies

#### Windows

Download the masscan executable from a reputable source or compile it yourself:

- Windows builds: [https://github.com/Arryboom/MasscanForWindows](https://github.com/Arryboom/MasscanForWindows)
- Official source: [https://github.com/robertdavidgraham/masscan](https://github.com/robertdavidgraham/masscan)

Install Npcap (recommended over the deprecated WinPcap):

- Npcap: [https://npcap.com/#download](https://npcap.com/#download)

Launch the game once. Server Scan will automatically create the configuration file at:

```bat
%APPDATA%\.minecraft\serverscan\config\masscan.conf
:: or replace %APPDATA%\.minecraft with your Minecraft directory if necessary
```

Open ```masscan.conf``` and set the path to your executable installation.

Example:

```bat
masscan.path=C:\\Users\\reallycoolperson\\Downloads\\masscan64.exe
```

Save the file and restart Minecraft if it is already running.

#### Linux

Install masscan using your distribution's package manager.

```bash
# Debian-based
apt install masscan

# Fedora/RHEL
dnf install masscan

# Arch Linux-based
pacman -S masscan
```

Grant the required raw socket capability permissions:

```bash
setcap cap_net_raw=ep $(which masscan)
```

Server Scan will automatically detect installations located in standard locations such as:
- ```/usr/bin/masscan```
- ```/usr/local/bin/masscan```

If automatic detection does not work, launch the game once and edit the configuration file manually with the exeuctable location:

```bash
<minecraft directory>/serverscan/config/masscan.conf
```

Example:

```bat
masscan.path=/home/reallycoolperson/bin/masscan
```

**Note:** On both operating systems, make sure to install Fabric API valid for the Server Scan version. A computer restart may be required.

## Features

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
