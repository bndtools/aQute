Freedns (http://freedns.afraid.org/)
This site provides free DNS hosting. amom=ng which a simple way to make sure
that your DNS is updated when your ADSL modem changes its public IP address again.
This bundle will periodically send a URL request to FreeDns which will then update
the DNS record.

You must register with Freedsn (surprisingly it is free for most use cases) and the
go to http://freedns.afraid.org/dynamic/. There you can add domains that you own (they
have a host of names that you can pick from). Copy the link on the Direct URL and
create a Factory Configuration for aQute.impl.freedns.Freedns


