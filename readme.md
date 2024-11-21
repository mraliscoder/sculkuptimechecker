# Sculk UC
Sculk UC - программа для проверки работоспособности запрашиваемого сервера.

## Системные требования
Для установки Sculk UC подойдет сервер с следующими характеристиками:
| Параметр                    | Значение                                            |
| --------------------------- | --------------------------------------------------- |
| Ядра процессора             | 1 или выше                                          |
| Оперативная память          | 512 МБ или выше                                     |
| Диск                        | 5 ГБ или выше*                                      |
| Пропускная способность сети | 256 kbps или выше                                   |
| Операционная система        | Ubuntu 20.04 / Ubuntu 22.04 / Debian 10 / Debian 11 |
| Дополнительно               | 1 публичный IPv4 адрес                              |

\* ПО сохраняет некоторую лог-информацию. При небольшом размере диска требуется периодически очищать логи. Они хранятся в директории `/opt/checker/logs/`.

## Установка
Для установки необходимо выполнить следующую команду от имени администратора (root): 
```bash
apt-get update && apt-get install curl -y && bash <(curl https://raw.githubusercontent.com/mraliscoder/sculkuptimechecker/master/install.sh)
```
Данная команда обновит список пакетов, установит curl и выполнит установочный скрипт.

## Добавление сервера в бота
После выполнения установочного скрипта он должен предоставить информацию, похожую на следующую:
```
 COPY THIS AND SEND TO CHAT.SCULK.RU/SUBMIT:
  Server IP: 1.1.1.1
  Security token: xxxxxxxxxxxxxxxxxxxxx
```
Для добавления Вам необходимо заполнить форму по ссылке: [https://chat.sculk.ru/submit](https://chat.sculk.ru/submit)

Вы должны будете заполнить поля "Адрес страницы ВКонтакте", "IP-адрес сервера", "Секретный ключ", "Страна" и "Город".
* _Адрес страницы ВКонтакте_ нужен для связи с Вами в случае обнаружения какой-либо неисправности. Это обязательное поле, к нему привязывается сам сервер. Адрес страницы хранится только в нашей базе данных и никому никогда не показывается.
* _IP-адрес сервера_ должен совпадать с полем `Server IP` из вывода скрипта установки.
* _Секретный ключ_ нужно скопировать из поля `Security token` из вывода скрипта установки.
* _Страна_ сервера должна быть указана точно. Мы дополнительно проверим ее через Whois. В случае несовпадения мы попросим у Вас доказательства, что сервер находится в указанной Вами стране.
* _Город_ сервера желательно указать, если Вы его знаете. Если не знаете - укажите столицу или прочерк, тогда мы попытаемся найти город сами.
* Также Вы можете указать _Рекламный текст_ - это небольшое (не больше 100 символов) поле после информации о сервере, где вы можете прорекламировать свой продукт. Так как мы не платим владельцам серверов за их предоставление, мы предоставляем возможность указать данный текст. Учтите, что данный текст не будет опубликован, если рекламируемые материалы содержат контент насилия, контент 18+ или запрещенный контент законами Российской Федерации. Также мы не пропускаем сокращенные ссылки или UTM-ссылки, их мы добавим по собственному желанию. В случае, если Вы хотите изменить данный текст, обращайтесь на любой контакт по ссылке [https://sculk.ru/support](https://sculk.ru/support).

## Обновление
Мы стараемся не выпускать обновления к данному ПО. Однако, если потребуется, мы свяжемся с Вами и попросим обновить приложение. Для обновления нужно выполнить данные команды:
```bash
wget "https://github.com/mraliscoder/sculkuptimechecker/releases/latest/download/uptimechecker.jar" -O /opt/checker/uptimechecker.jar
systemctl restart sculkuptime
```

## Команды
У сервиса нет команд, однако его можно в случае чего остановить, перезапустить и просто запустить. Это делается через `systemctl`:
* Запуск: `systemctl start sculkuptime`
* Остановка: `systemctl stop sculkuptime`
* Перезапуск: `systemctl restart sculkuptime`

## Удаление
Если Вы решите удалить приложение, перед этим не забудьте уведомить нас об этом: [https://sculk.ru/support](https://sculk.ru/support).

Для удаления выполните следующие команды:
```bash
systemctl stop sculkuptime
systemctl disable sculkuptime
rm -rf /opt/checker
rm -f /etc/systemd/system/sculkuptime.service
```

## API
Сервис работает на порту 9002. Обращение происходит посредством HTTP POST запросов на адрес `http://<IP_сервера>:9002/`. Авторизация осуществляется посредством поля `Authorization` в заголовках запроса. Оно должно выглядеть как `Authorization: Bearer <secret_token>`. Тип контента (`Content-Type`) - `application/json`.

**POST /ping**

Выполнить 4 пинга к серверу и предоставить результат.

_Запрос:_
```json
{
	"host": "1.2.3.4" // IP-адрес или домен
}
```

_Ответ_:
```json
{
	"success": true,
	"version": "1.0.4", // версия Sculk UC
	"response": {
		"result": true, // true или false. false - если ни один пинг не был успешным
		"percent": 50, // процент успешных пингов, 0, 25, 50, 75 или 100.
		"attempts": [ // массив описывает попытки пингов
			{
				"attempt": 1, // номер попытки
				"available": true, // индикатор доступности (пинг успешный)
				"ping": 3 // время пинга
			},
			{
				"attempt": 2,
				"available": false // сервер недоступен (нет ответа в установленное время)
			},
			{
				"attempt": 3,
				"available": true,
				"ping": 6
			},
			{
				"attempt": 4,
				"available": false
			}
		],
		"average": 2.5 // средний результат успешных пингов
	}
}
```

**POST /http**

Выполняет HTTP-запрос к указанному URL и возвращает код HTTP

_Запрос:_
```json
{
	"url": "https://edwardcode.net" // URL, который необходимо проверить
}
```

_Ответ:_
```json
{
	"success": true,
	"version": "1.0.4", // версия Sculk UC
	"response": {
		"time": 200, // Время, затраченное на выполнение запроса
		"code": 301, // HTTP-код, отправленный удаленным сервером
		"result": true // true|false - статус проверки. false, если не получилось соединиться с сервером в указанное время
		"error": "" // текст ошибки, если result: false
	}
}
```

_Ответ в случае ошибки (применим ко всем методам):_
```json
{
	"success": false,
	"error": 403, // код ошибки
	"errorDescription": "Unauthorized", // описание ошибки
	"version": "1.0.4" // версия Sculk UC
}
```

## Copyright
(C) 2022 - 2024 [edwardcode](https://edwardcode.net)
