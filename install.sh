#!/bin/bash

BLACK='\033[0;30m'
RED='\033[0;31m'
GREEN='\033[0;32m'
BROWN='\033[0;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
LIGHTGRAY='\033[0;37m'
DARKGRAY='\033[1;30m'
LIGHTRED='\033[1;31m'
LIGHTGREEN='\033[1;32m'
YELLOW='\033[1;33m'
LIGHTBLUE='\033[1;34m'
LIGHTPURPLE='\033[1;35m'
LIGHTCYAN='\033[1;36m'
WHITE='\033[1;37m'
NC='\033[0m'
BOLD='\033[1m'

loginfo () {
	echo -e "${WHITE}${BOLD}"$1"${NC}"
}

clear
echo -e ""
echo -e "${WHITE}------------ ${YELLOW}XOREK.CLOUD ${WHITE}------------${NC}"
echo -e "${WHITE}      Sculk UC Installation${NC}"
echo -e "${WHITE}-------------------------------------${NC}"
echo -e ""

if [ "$EUID" -ne 0 ]
  then loginfo "Please run script as root"
  exit
fi

loginfo "Installing curl and JRE"
apt-get update && apt-get install curl openjdk-17-jre

loginfo "Preparing for installation..."
mkdir /opt/checker

loginfo "Downloading Sculk UC..."
curl "https://github.com/dpkgsoft/sculkuptimechecker/releases/latest/download/uptimechecker.jar" -o /opt/checker/uptimechecker.jar

loginfo "Creating service..."
curl "https://raw.githubusercontent.com/dpkgsoft/sculkuptimechecker/master/sculkuptime.service" -o /etc/systemd/system/sculkuptime.jar

loginfo "Starting service..."
systemctl daemon-reload
systemctl enable --now sculkuptime

loginfo "Waiting 5 seconds to let service start..."
sleep 5

SERVERIP=$(curl -s -4 ens4.ru)
SECURITY_TOKEN=$(cat /opt/checker/token.txt)

echo -e ""
echo -e "${CYAN} COPY THIS AND SEND TO CHAT.SCULK.RU/SUBMIT:${NC}"
echo -e "  Server IP: ${SERVERIP}"
echo -e "  Security token: ${SECURITY_TOKEN}"
echo -e ""

exit
