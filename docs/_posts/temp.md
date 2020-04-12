# 안드로이드 시스템 개발자를 위한 안드로이드 시스템의 분석 및 이해

안드로이드는 시스템의 안정성을 위하여 하드웨어 제어 등의 기능을 다음과 같이 두 부분으로 분리하였다.
● Server: Service(하드웨어제어 등)을 제공하는 부분
● Client: 하드웨어에 접근하기 위해 Server에 요청한다. (상호간 양방향 통신도 가능하다.)

서비스 개발 요점
핵심 서비스는 일반적으로 별도의 프로세스에서 실행된다. IBinder 인터페이스를 제공 해야 한다. 즉, 바인터 클래스인 IBinder를 사용하여 프로세스간에 통신을 할 수 있다. 또한 원활한 서비스 제공을 위해 쓰레드를 유지할 필요가 있다. 핵심 서비스를 사용하기 전에 Binder Driver에 추가 되어야 한다. Binder Driver를 위하여 Service Manager에서 핵심 서비스를 추가할 수 있다. 또한, Binder Driver에서 Service Manager가 서비스를 받을 수 있다.

서비스의 구현
서비스를 ServiceManager에 등록하고 트랜잭션을 처리해주는 서비스를 만든다.
다음 [그림1]과 같이 ServiceManager에 등록한 서비스가 추가된다. [리스트1] 소스에서는 단지 AddService 추가 작업을 한다. AddService는 Binder Driver에 추가될 핵심 서비스이다. C++을 사용하여 AddService 클래스 정의를 제공하고 있다.

```cpp

#include <sys/types.h>
#include <unistd.h>
#include <grp.h>

#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h>
#include <binder/IServiceManager.h>
#include <utils/Log.h>

#include <private/android_filesystem_config.h>

#include "../core_service/addservice.h"
using namespace android;

int main (int argc, char ** argv)
{
sp <ProcessState> proc(ProcessState::self());
sp <IServiceManager> sm = defaultServiceManager ();
LOGE ( "ServiceManager : %p", sm.get());
AddService::instantiate();
ProcessState::self()->startThreadPool();
IPCThreadState::self()->joinThreadPool();
}

```

클라이언트의 구현
등록되어 있는 서비스에 접근하여 데이터를 주고 받는다. setN() 메서드는 ServiceManager 인터페이스를 얻기 위하여 getAddService ()를 호출한다. ServiceManager 인터페이스를 사용하여 AddService 서비스를 할당해 Service Manager 묶기에 성공하면 ServiceManager는 BpBinder의 IBinder 인터페이스를 제공한다. 인터페이스 참조 binder 변수에 저장된

```cpp

// AddService.h
#ifndef ANDROID_GUILH_ADD_SERVICE_H
#define ANDROID_GUILH_ADD_SERVICE_H
#include <utils/RefBase.h>
#include <binder/IInterface.h>
#include <binder/Parcel.h>

namespace android {
class AddService : public BBinder
{
public:
static int instantiate();
AddService();
virtual ~AddService();
virtual status_t onTransact(uint32_t, const Parcel&, Parcel*, uint32_t);
};
}; //namespace
#endif

```

서비스 예제 정의 파일

```cpp

// AddService.cpp
#include "addservice.h"
#include <binder/IServiceManager.h>
#include <binder/IPCThreadState.h>

namespace android
{
static struct sigaction oldact;
static pthread_key_t sigbuskey;

int AddService::instantiate()
{
LOGE("AddService instantiate");
int r = defaultServiceManager()->addService( String16("jhc.add1"), new AddService() );
LOGE("AddService r = %dn", r);
return r;
}

AddService::AddService()
{
LOGE("AddService created");
pthread_key_create(&sigbuskey, NULL);
}

AddService::~AddService()
{
pthread_key_delete(sigbuskey);
LOGE("AddService destroyed");
}

status_t AddService::onTransact(uint32_t code, const Parcel& data, Parcel* reply, uint32_t flags)
{
switch(code) {
case 0:
{
pid_t pid = data.readInt32();
int num = data.readInt32();
num = num + 1000;
LOGE("server : num = %d",num);
reply->writeInt32(num);
return NO_ERROR;
} break;
default:
return BBinder::onTransact(code, data, reply, flags);
}
}
}; //namespace

```

--------------------------------------------------------

#include <binder/IServiceManager.h>
#include <binder/IPCThreadState.h>
#include <utils/Log.h>
#include "ADD.h"

namespace android {
sp<IBinder> binder;
void Add::setN(int n)
{
getAddService();
Parcel data, reply;
data.writeInt32(getpid());
data.writeInt32(n);
LOGE("BpAddService::create remote()->transact()n");
binder->transact(0, data, &reply);
return;
}

const void Add::getAddService()
{
sp<IServiceManager> sm = defaultServiceManager();
binder = sm->getService(String16("jhc.add1"));
LOGE("Add::getAddService %pn",sm.get());
if (binder == 0)
{
LOGW("AddService not published, waiting...");
return;
}
}
}; //namespace

---------------------[리스트 4] 서비스 실제 구현 예제





Binder 클래스
바인더에 관련된 전체 클래스의 구조는 [그림3]와 같다.


[그림 3]


메모리 관련 소스들
바인더와 연동이 되는 여러 개의 메모리 관련 클래스들이 다음과 같이 존재한다. IMemory.h-IMemoryHeap과 BnMe moryHeap class와 관련된 메모리 인터페이스에 대한 정의와 일반적인 메모리 class인 IMemory와 BnMemory에 대한 내용이 있다.

● MemoryHeapBase.h-BnMemoryHeap으로부터 상속된 MemoryHeap에 대한 정의
● MemoryBase.h-BnMemory로부터 상속된 Memory Base에 대한 정의

일반적으로 프로세스에 있어 힙 메모리(eap memory)를 사용하는 것은 MemoryHeapBase 사용에 대한 것을 기본으로 한다(malloc과 같은). 이와는 대조적으로 MemoryBase는 하나의 메모리로부터 이미 할당된 힙 메모리를 사용한다. 이외에 메모리 관련 함수들은 MemoryDealer.h와 Memory HeapPmem.h에 정의되어 있다.

바인더의 RPC 구조
● IInterface.h - Binder Interface관련 템플릿이 지정된 header
● BnInterface
● BpInterface
● BnInterface 템플릿
● Native Interface작성시 사용
● 바인더의 서버파트 코드에서 사용됨
● BpInterface 템플릿
● Proxy Interface 작성시 사용
● 바인더의 클라이언트 파트 코드에서 사용됨

사실상 두 템플릿이 사용 될 때는 템플릿으로부터 상속받아서 새로운 클래스를 생성하여 사용하게 되고, 각각 서로 각 프로세스의 다른 편 접속 창구의 역할을 하게 된다.

--------------------------------------------------------

template<typename INTERFACE>
class BnInterface : public INTERFACE, public BBinder
{
public:
virtual sp<IInterface> queryLocalInterface(const String16& _descriptor);
virtual String16 getInterfaceDescriptor() const;
protected:
virtual IBinder* onAsBinder();
};
template<typename INTERFACE>
class BpInterface : public INTERFACE, public BpRefBase
{
public:
BpInterface(const sp<IBinder>& remote);
protected:
virtual IBinder* onAsBinder();
};

--------------------------------------------------------

Binder 사용 Interface선언
사용자 정의 Binder interface인 Bp로 시작되는 Class를 만들 때 INTERFACE에서 상속된 두 템플 BnInterface와 BpInterface를 사용한다. BpInterface를 이용해서 Proxy Class를 선언할 경우는 일반적인 방법으로 선언하지 않고 매크로를 이용해서 선언한다.

BpXXX Interface선언 방법
● DECLARE_META_INTERFACE
● IMPLEMENT_META_INTERFACE
● 위의 두 개의 macro를 이용해서 선언

BnInterface를 이용해서 Native Class를 선언할 경우는 BnXXX의 형태로 직접 선언한다. 그러므로, Binder관련 IPC에 대한 코드를 분석할 경우는 BpXXX의 인터페이스는 관련 Macro를 조사해야 한다.
사용자 정의 proxy interface인 BpXXX를 정의할 때는 두 개의 매크로를 이용해서 선언이 된다.

--------------------------------------------------------

define DECLARE_META_INTERFACE(INTERFACE) ￦
static const String16 descriptor; ￦
static sp<I##INTERFACE> asInterface(const sp<IBinder>& obj); ￦
virtual String16 getInterfaceDescriptor() const; ￦
#define IMPLEMENT_META_INTERFACE(INTERFACE, NAME) ￦
const String16 I##INTERFACE::descriptor(NAME); ￦
String16 I##INTERFACE::getInterfaceDescriptor() const { ￦
return I##INTERFACE::descriptor; ￦
} ￦
sp<I##INTERFACE> I##INTERFACE::asInterface(const sp<IBinder>& obj) ￦
{
... ￦
return intr; ￦
}

--------------------------------------------------------

BpInterface는 위의 매크로에서 볼 수 있듯이 asInterfa ce()와 getInterfaceDescriptor()함수만 설정함으로써 만들어질 수 있다.

바인더의 기본 동작


[그림 4]

바인더를 동작시키기 위해서는 앞에서 설명한 libbinder .so, service manager 등을 이용한다. service manager는 프로세서 커뮤니케이션이 필요한 두 개의 프로세스에 서비스를 시작하게 해주는 복잡한 데몬 프로세스(daemon process)이다. 두 개의 프로세스는 libbinder.so 를 호출함으로써 통신이 이루어지며, 실제 통신에서는 커널 스페이스의 공유 메모리 영역을 사용하게 된다

Application 입장에서의 바인더
application 입장에서 바라봤을 경우에는 바인더에 대해 2개의 관점으로 볼 수 있다.

● Native local - BnABC, 이 클래스를 실제로 구현하기 위해서는 상속받는 것이 필요하다.
● Proxy agent- BpABC, 실현되어 있는 인터페이스에 대한 프레임웍이다. 하지만 인터페이스는 클래스를 그대로 반영하지는 않는다
● client-실제로 BpABC를 호출했을 때 client interface ABC가 된다.
● Native함수(local)인 Bn이 맡는 부분 - IServiceManager:: AddService() 를 이용해서 service를 등록해 BnABC::on Transact()을 실행한다.
● Proxy함수인 Bp가 맡는 부분- client의 요청을 받아서 해당 함수를 실행한다.
BpABC::remote()->transact() 함수를 호출(해서 접속함)

● client가 맡는 부분- ABC에 대한 aceess를 획득하기 위하여, 인터페이스 함수를 호출한다. 이 함수들은 사실은 Bp ABC를 호출하는 것이다. 그리고 나서 BnABC로의 IPC 통신을 하게된다. 그리고, client의 동작에 관련된 함수들을 호출한다.
프로세스의 생성이 끝나면 BnABC와 BpABC 인터페이스는 ABC를 상속한다.

● BpABC의 동작(Service user) - BpABC는 일반적인 함수 호출에서와 같이 실행. 구현된 class는 실제 통신에의 응답을 반영하도록 작성하지만 실제 함수로는 구현되지 않는다. 즉, 통신개념이기 때문에 실제의 코드 동작은 BnABC쪽에서 이루어진다.
● BnABC의 동작(Server provider)- BnABC는 실제로 맡은 일을 수행하는 부분의 코드이다. 이를 위해서 Class상속이 실제로 필요한 인터페이스 클래스, BpABC 요청에 따른 처리를 하기 위해 구현된 실제 동작함수들로 이루어져 있다. BpABC의 요청에 따른 응답을 reply값으로 돌려준다. return값과 reply값은 다른 개념이다. return 값은 실제 함수의 return값이다. (BnABC가 BpABC로 돌려주는 경우가 많다). reply 값은 BnABC에서 BpABC로 응답에 대한 답을 돌려주는 것이다. 보통은 이 두 개의 값을 동일하게 처리한다.

IServiceManager로부터 service를 획득한 client ABC인터페이스는 client interface를 호출한다.
실제 이 호출은 BpABC에 대한 호출이다. 바인더의 IPC 메커니즘을 통해서 BpABC와 BnABC는 통신을 하게 되고, BnABC는 실제 특정 타입에 대한 기능을 실현하게 된다. 두 개의 프로세스는 서버와 클라이언트의 관계로 실제 단단하게 연결이 맺어지게 된다. 이것은 process간의 통신으로 생각할 필요가 없이 client에서 직접적으로 프로세스간의 함수를 호출하는 것처럼 보인다. 물론, 이 함수들은 반드시 ABC에 정의되어 있어야 한다.

● IServiceManager 동작- IServiceManager는 IService Manager.h와 IServiceManager.cpp에 해당 코드가 있다. IServiceManager는 ServiceManager daemon으로부터 실행이 되며, 사용자 프로그램은 BpServiceManager를 통해서 다른 서비스를 요청할 수 있다. IServiceManager.h 에는 다음과 같은 default IServiceMa nager 인터페이스가 정의되어 있다.

sp<IServiceManager> defaultServiceManager();

실제 구현 예를 통한 Binder의 구조 파악
PermissionController-인터페이스 접근제어를 담당
libutils에 있는 다음과 같은 두 개의 파일에 정의되어 있음
IPermissionController.h
IPermissionController.cpp
IPermissionController.h- 주요 인터페이스에 대한 정의가 되어 있으며 주로 Binder관련
인터페이스가 정의되어 있음. IInterface로부터 상속받은 Bp 인터페이스인 IPermissionController와 IPermission Controller로부터 상속받은 BnPermissionController에 대한 클래스에 대한 정의가 되어 있음

IPermissionController 에 대한 Class선언은 다음과 같다.

--------------------------------------------------------

class IPermissionController : public Iinterface {
public:
DECLARE_META_INTERFACE(PermissionController);
virtual bool checkPermission(const String16& permission,
int32_t pid, int32_t uid) = 0;
enum {
CHECK_PERMISSION_TRANSACTION = IBinder::FIRST_ CALL_TRANSACTION }; };
class BnPermissionController : public BnInterface<IPermission Controller> {
public:
virtual status_t onTransact( uint32_t code, const Parcel& data, Parcel* reply, uint32_t flags = 0); };

--------------------------------------------------------

IPermissionController.h
IPermissionController class안의 DECLARE_META_ INTERFACE 매크로의 사용은 Bp쪽 인터페이스를 정의한다.

--------------------------------------------------------

enum {
CHECK_PERMISSION_TRANSACTION = IBinder::FIRST_CALL_TRANSACTION
};

--------------------------------------------------------

위와 같이 선언된 부분은 Bp 인터페이스와 Bn 인터페이스에서 서로 통신에 사용하는 user defined command를 나타낸다. IPermissionController class는 순수 virtual 함수의 형태를 갖는 checkPermission()만을 멤버로 가지고 있다. BnPermissionController 는 BnInterface라는 템플릿으로부터 상속한다. BnPermissionController는 실제로 BBinder와 IPermissionController class로부터 이중으로 상속이 된 것이다.

IPermissionController.cpp
IPermissionController class에 선언된 것을 구현한다. BpPermissionController Binder의 proxy 코드로서 client process에서 호출되는 함수를 정의 호출되는 함수는 해당 command와 data를 Bn 인터페이스로 binder를 통해서 전송한다. IMPLEMENT_META_INTERFACE 매크로를 이용하여 인터페이스를 생성한다.

BnPermissionController
BnPermissionController는 IPermissionController로부터 상속되었기 때문에, 순수 virtual 함수인 checkPermi ssion()이 아직 실체화 되지 않은 상태이다.
BnPermissionController는 여기서는 초기화 되지 않으며 실제적으로는 인터페이스만 존재하게 된다. 이것은 상속을 받을 때 실제로 실체화가 되며 특정한 실제 함수로 세팅이 되게 된다.

IPermissionController.h에서 선언된 class는 IPermi ssionController.cpp에서 구현된다.

--------------------------------------------------------

class BpPermissionController : public BpInterface<IPermission Controller> {
public:
BpPermissionController(const sp<IBinder>& impl)
: BpInterface<IPermissionController>(impl) { }
virtual bool checkPermission(const String16& permission, int32_t pid, int32_t uid) {
Parcel data, reply; // data 전송을 위한 Parcel class를 이용하였음
data.writeInterfaceToken(IPermissionController::getInterfaceDescriptor());
data.writeString16(permission);
data.writeInt32(pid);
data.writeInt32(uid);
remote()->transact(CHECK_PERMISSION_TRANSACTION, data, &reply);
// fail on exception
if (reply.readInt32() != 0) return 0;
return reply.readInt32() != 0; } };
IMPLEMENT_META_INTERFACE(PermissionController, "android.os.IPermissionController");

--------------------------------------------------------

서버 파트에서 호출되는 BnPermissionController의 onTransact() 함수는 다음과 같이 구현이 되었다.

--------------------------------------------------------

status_t BnPermissionController::onTransact(uint32_t code, const Parcel& data, Parcel* reply, uint32_t flags) {
//printf("PermissionController received: "); data.print();
switch(code) {
case CHECK_PERMISSION_TRANSACTION: {
CHECK_INTERFACE(IPermissionController, data, reply);
String16 permission = data.readString16();
int32_t pid = data.readInt32();
int32_t uid = data.readInt32();
//==> server쪽의 실체 함수 호출
bool res = checkPermission(permission, pid, uid);
// write exception
reply->writeInt32(0);
reply->writeInt32(res ? 1 : 0); // 실체함수에서의 return값을 return
return NO_ERROR;
} break;
default:
return BBinder::onTransact(code, data, reply, flags); } }

--------------------------------------------------------

Binder서비스의 시작
BnABC 서비스의 시작- 프로세스에 대한 보호처리를 끝낸 후에 시작되는 local service는 BnABC 상속에 의해 실제 class가 실체화 됨으로써 제공되게 된다. 서비스의 이름은 일반적으로 ABC 라고 이름을 붙이게 된다. 보통 ABC라는 service는 instantiate()라는 함수를 포함하고 있으며, 이 함수는 일반적으로 다음과 같은 형태가 된다.

--------------------------------------------------------

void ABC::instantiate() {
defaultServiceManager()->addService(
String16("XXX.ABC"), new ABC ());
}

--------------------------------------------------------

위와 같은 방법으로 defaultServiceManager()을 호출함으로써, "XXX.ABC" services를 기존의 서비스들에 추가하게 됨.

BpABC 접속의 시작
BpABC는 주로 데이터를 전송하기 위하여 mRemote->transact()를 호출하는 과정을 거친다.
mRemote는 BpRefBase 클래스의 멤버이다. 이것이 IBinder이며, 실제 함수가 호출되는 과정의 흐름은 다음과 같다.

--------------------------------------------------------

mRemote () -> transact ()
Process:: self ()
IPCThreadState:: self () -> transact ()
writeTransactionData ()
waitForResponse ()
talkWithDriver ()
ioctl (fd, BINDER_WRITE_READ, & bwr)

--------------------------------------------------------

IPCThreadState:: executeCommand ()함수에서 실제 전송이 이루어진다.
Service Provider(server라고 표현)는 service manager에 등록을 해서 서비스 할 수 있는 대기상태로 존재하게 된다. defaultServiceManager()는 다음과 같은 함수들을 호출하게 된다.

--------------------------------------------------------

ProcessState::self()->getContextObject(NULL));
IPCThreadState* ipc = IPCThreadState::self();
IPCThreadState::talkWithDriver()

--------------------------------------------------------
ProcessState 클래스는 생성자에서 open_driver()를 호출해서 talkWithDriver()를 구현한 프로세스에 접근할 수 있는 통로를 설정하게 된다.