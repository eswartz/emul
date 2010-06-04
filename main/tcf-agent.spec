%define name tcf-agent
%define version 0.3.0
%define release 1.%(bin/get-os-tag)

Name: %{name}
Summary: Target Communication Framework agent
Version: %{version}
Release: %{release}
Vendor: eclipse.org
Source: http://dev.eclipse.org/svnroot/dsdp/org.eclipse.tm.tcf/trunk/srpms/%{name}-%{version}.tar.bz2
URL: http://wiki.eclipse.org/DSDP/TM/TCF_FAQ
Group: Development/Tools/Other
BuildRoot: %{_tmppath}/%{name}-buildroot
License: EPL

%description
Target Communication Framework is universal, extensible, simple,
lightweight, vendor agnostic framework for tools and targets to
communicate for purpose of debugging, profiling, code patching and
other device software development needs. tcf-agent is a daemon,
which provides TCF services that can be used by local and remote clients.

%prep
rm -rf $RPM_BUILD_ROOT
%setup

%build
make all CONF=Release

%install
make install CONF=Release INSTALLROOT=$RPM_BUILD_ROOT SBIN=%{_sbindir}

%clean
[ "$RPM_BUILD_ROOT" != "/" ] && rm -rf $RPM_BUILD_ROOT

%post
%{_sbindir}/tcf-agent -c
chkconfig --add %{name}
/sbin/service %{name} start > /dev/null 2>&1 || :

%postun
if [ $1 -ge 1 ] ; then
  /sbin/service %{name} condrestart > /dev/null 2>&1 || :
fi

%preun
if [ "$1" = 0 ] ; then
  /sbin/service %{name} stop > /dev/null 2>&1 || :
  chkconfig --del %{name}
fi

%files
%defattr(-,root,root,0755)
%config /etc/init.d/%{name}
%{_sbindir}/%{name}

%changelog
* Thu Jun 03 2010 Eugene Tarassov <eugene.tarassov@windriver.com> 0.3.0
- Eclipse 3.6.0 Helios release
* Thu Mar 12 2009 Eugene Tarassov <eugene.tarassov@windriver.com> 0.0.1
- first release
